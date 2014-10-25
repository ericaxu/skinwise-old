import sys

from util import (web, db, parser, util)

db = db.DB("cache/paula.cache.db")
crawler = web.Crawler(db)

#File out
file_products_paula_json = "data/products.paula.json.txt"
#Crawled URLs
url_product_page = "http://www.paulaschoice.com/beautypedia-skin-care-reviews/by-brand/%s"
url_product_list = "http://www.paulaschoice.com/beautypedia-skin-care-reviews?sort=product&direction=asc&pageSize=100&pageNumber=%d"

replace_dict = {
	r'aqua \(water\) eau\)': "aqua (water) eau",
	r', iron oxides\)\.': ", iron oxides."
}
# $replace_rules_str = array(
# 	"aqua (water) eau)" => "aqua (water) eau",
# 	", iron oxides)." => ", iron oxides.",
# 	"active ingredients:" => ",",
# 	"other ingredients:" => ",",
# 	"active:" => ",",
# 	"other:" => ",",
# 	"(and)" => ",",
# 	";" => ","
# );

print("Searching product urls")

# Find max page number
index_html = crawler.crawl_selective(key="list/1", url=url_product_list % 1, regex=r'id="main"(.*?)Skip to Top')
index_table = parser.regex_find(r'<select name="[^"]*pageNumberList"(.*?)<\/select>', index_html).group(1)
index_rows = parser.regex_find_all("<option[^>]*>(.*?)<\\/option>", index_table)
numpages = int(index_rows[-1])

# Get a list of product urls
urls = list()
for i in range(1, numpages):
	page_html = crawler.crawl_selective(key="list/%d" % i, url=url_product_list % i, regex=r'id="main"(.*?)Skip to Top')
	page_table = parser.regex_find(r'Size<\/th>(.*)<\/tbody>', page_html).group(1)
	page_rows = parser.regex_find_all(r'<tr>(.*?)<\/tr>', page_table)

	for page_row in page_rows:
		page_cols = parser.regex_find_all(r'<td>(.*?)<\/td>', page_row)
		# 0 = checkbox
		# 1 = rating
		# 2 = product
		# 3 = brand
		# 4 = category
		# 5 = price
		# 6 = size
		product_link = parser.regex_find(r'href="/beautypedia-skin-care-reviews/by-brand/([^"]*)"', page_cols[2]).group(1)

		urls.append(product_link)

db.commit()
urls = util.list_unique(urls)

print("Fetching products")

result = dict()
result['products'] = dict()

for url in urls:
	page_html = crawler.crawl_selective(key="product/%s" % url, url=url_product_page % url, regex=r'id="main"(.*?)Skip to Top')

	page_table = parser.regex_find(r'<div class="grid-row clearfix">(.*?)id="leavingSite"', page_html, 1)

	product_name = parser.regex_find(r'<div class="u-miscellaneous-pagetitle clearfix">(.*?)<\/div>', page_table, 1)
	product_name = parser.strip_tags(product_name)

	product_brand = parser.regex_find(r'<div class="brand">by <a[^>]*>(.*?)<\/a>', page_table, 1)
	product_brand = parser.strip_tags(product_brand)

	if not product_name or not product_brand:
		continue

	product_claims = parser.regex_find(r'<div id="[^"]*pnlTabBodyClaims"[^>]*>(.*?)<\/div>', page_table, 1)
	product_claims = parser.strip_tags(product_claims)

	product_ingredients = parser.regex_find(r'<div id="[^"]*pnlTabBodyIngredients"[^>]*>(.*?)<\/div>', page_table, 1)

	product_category = parser.regex_find(r'<a id="[^"]*hlCategory"[^>]*>(.*?)<\/a>', page_table, 1)

	product_key_ingredients = parser.regex_find(r'<dd[^>]*>(.*?)<\/dd>', product_ingredients, 1)
	product_other_ingredients = parser.regex_find(r'<p[^>]*>(.*?)<\/p>', product_ingredients, 1)

	product_key_ingredients = parser.strip_tags(product_key_ingredients)
	product_other_ingredients = parser.strip_tags(product_other_ingredients)

	product_key_ingredients = parser.regex_replace_dict(replace_dict, product_key_ingredients)
	product_other_ingredients = parser.regex_replace_dict(replace_dict, product_other_ingredients)

	product = dict()
	product['name'] = web.html_unescape(product_name)
	product['brand'] = web.html_unescape(product_brand)
	product['type'] = web.html_unescape(product_category)
	product['description'] = web.html_unescape(product_claims)
	product["key_ingredients"] = web.html_unescape(product_key_ingredients)
	product['ingredients'] = web.html_unescape(product_other_ingredients)

	key = parser.product_key(product['brand'], product['name'])

	result['products'][key] = product

util.json_write(result, file_products_paula_json)

parser.print_count(result['products'], "Products")
