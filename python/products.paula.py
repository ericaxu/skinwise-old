import sys

from util import (web, db, parser, util)

db = db.DB("cache/paula.cache.db")
crawler = web.Crawler(db)

#File in
file_product_paula_type_corrections_json = "data/products.paula.type.corrections.json.txt"
#File out
file_products_paula_json = "data/products.paula.json.txt"
#Crawled URLs
url_product_page = "http://www.paulaschoice.com/beautypedia-skin-care-reviews/by-brand/%s"
url_product_list = "http://www.paulaschoice.com/beautypedia-skin-care-reviews?sort=product&direction=asc&pageSize=100&pageNumber=%d"

ingredient_corrections = {
	r'Aqua \(Water\) Eau\)': "Aqua (Water) Eau",
	r', Iron Oxides\)\.': ", Iron Oxides.",
	r'\s*\(listed in alphabetical order per the new FDA sunscreen monograph\)': "",
	r'(?i)sa;icylic acid': "Salicylic Acid",
	r'<strong>\s*:\s*</strong>': ":",
	r'<strong>\s*(?i)May Contain:\s*</strong>': "May Contain:",
	r'<strong>\s*(?i)Others* *:\s*</strong>': "Other:",
	r'<strong>\s*(?i)Actives* *:\s*</strong>': "Other:",
	r'\(this is the version we do NOT recommend\):': ""
}

description_corrections = {
	r'>/p>': "",
	r'/p>': "",
	r'p>': ""
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

type_corrections = util.json_read(file_product_paula_type_corrections_json, "{}")
result = dict()
result['products'] = dict()

for url in urls:
	page_html = crawler.crawl_selective(key="product/%s" % url, url=url_product_page % url, regex=r'id="main"(.*?)Skip to Top')

	page_table = parser.regex_find(r'<div class="grid-row clearfix">(.*?)id="leavingSite"', page_html, 1)

	product_name = parser.regex_find(r'<div class="u-miscellaneous-pagetitle clearfix">(.*?)<\/div>', page_table, 1)
	product_name = web.html_unescape(parser.strip_tags(product_name))

	product_brand = parser.regex_find(r'<div class="brand">by <a[^>]*>(.*?)<\/a>', page_table, 1)
	product_brand = web.html_unescape(parser.strip_tags(product_brand))

	if not product_name or not product_brand:
		continue

	product_claims = parser.regex_find(r'<div id="[^"]*pnlTabBodyClaims"[^>]*>(.*?)<\/div>', page_table, 1)
	product_claims = web.html_unescape(parser.strip_tags(product_claims))
	product_claims = parser.fix_space(parser.regex_replace_dict(description_corrections, product_claims))

	product_category = parser.regex_find(r'<a id="[^"]*hlCategory"[^>]*>(.*?)<\/a>', page_table, 1)
	if product_category in type_corrections:
		product_category = type_corrections[product_category]

	product_price_size = parser.regex_find(r'<dt>Price:</dt><dd>([^<]*)</dd>', page_table, 1)
	product_price_size = web.html_unescape(product_price_size).replace('\u00a0', ' ')
	product_price = product_price_size.strip()
	product_size = ""
	if '-' in product_price_size:
		pieces = product_price_size.split('-')
		product_price = pieces[0].strip()
		product_size = pieces[1].strip()

	product_ingredients = parser.regex_find(r'<div id="[^"]*pnlTabBodyIngredients"[^>]*>(.*?)<\/div>', page_table, 1)
	product_ingredients = web.html_unescape(parser.regex_remove(r'<strong>\\xa0<\/strong>', product_ingredients))

	product_key_ingredients = parser.regex_find(r'<dd[^>]*>(.*?)<\/dd>', product_ingredients, 1)
	product_other_ingredients = parser.regex_find_all(r'<p[^>]*>(.*?)<\/p>', product_ingredients)

	def getIngredients(ingredients):
		ingredients = parser.regex_replace(r' (?i)Ingredients*:', ":", ingredients).strip()

		if ingredients.startswith(":"):
			ingredients = ingredients[1:]
		key = ""
		other = ""

		ingredients = parser.fix_space(parser.strip_tags(ingredients))

		other = parser.regex_find(r'(?i)(Other|Inactive) *:', ingredients)
		if other is None:
			other = parser.regex_remove(r'^(?i)Actives* *:', ingredients).strip()
		else:
			split = parser.regex_split(r'(?i)(Other|Inactive) *:', ingredients)
			key = parser.regex_remove(r'^(?i)Actives* *:', split[0]).strip()
			other = split[-1].strip()

		return (key, other)

	def createProduct(name, key_ingredients, ingredients):
		product = dict()
		product['name'] = name
		product['brand'] = product_brand
		product['types'] = product_category
		product['description'] = product_claims
		product['price'] = product_price
		product['size'] = product_size
		product["key_ingredients"] = key_ingredients
		product['ingredients'] = ingredients

		key = parser.product_key(product['brand'], product['name'])

		result['products'][key] = product

	# No other ingredients
	if len(product_other_ingredients) < 1:
		product_other_ingredients = ""
	else:
		# Product Systems
		if len(product_other_ingredients) > 1:
			title = parser.regex_find(r'<strong>(.*?)<\/strong>(|<br \/>)', product_other_ingredients[0], 1)
			#print("%r" % (product_other_ingredients))
			print("")
		else:
			pass

		product_other_ingredients = product_other_ingredients[0]
		product_other_ingredients = parser.regex_replace_dict(ingredient_corrections, product_other_ingredients)
		product_other_ingredients = parser.regex_remove(r'<strong>(.*?)<\/strong>', product_other_ingredients)
		key_ingredients, ingredients = getIngredients(product_other_ingredients)
		if key_ingredients != "":
			product_key_ingredients = key_ingredients
		product_other_ingredients = ingredients

	createProduct(product_name, product_key_ingredients, product_other_ingredients)

util.json_write(result, file_products_paula_json)

parser.print_count(result['products'], "Products")
