import sys

from util import (web, db, parser, util)

db = db.DB("cache/cosing.cache.db")
crawler = web.Crawler(db)


#File in
file_products_cosing_type_corrections_json = "data/products.cosing.type.corrections.json.txt"
file_products_brand_corrections_json = "data/products.brand.corrections.json.txt"
#File out
file_products_cosing_json = "data/products.cosing.json.txt"
#Crawled URLs
url_root = "http://www.cosmetic-ingredients.net/"
url_index = url_root + "index.php"
url_brand = url_root + "product.php?type=%%27%%20OR%%20%%271%%27=%%271&brand=%d"

ingredient_corrections = {
	r'\(Vaccinium Myrtillus \(Bilberry\) Extract': "Vaccinium Myrtillus (Bilberry) Extract",
	r', Organic Lemon Peel Oil\)': ", Organic Lemon Peel Oil",
	r' Ishea Butter\)': " (shea Butter)",
	r'Fragrance Iparfum\)': "Fragrance (parfum)"
}

print("Parsing brands and products")

index_html = crawler.crawl_selective(key="index", url=url_index, 
			regex=r'Ingredient Search: ingredient 1(.*?)<img src="resources/welcome.png">')

index_table = parser.regex_find(r'<select name=[^>]*>(.*?)</select>', index_html, 1)
index_rows = parser.regex_find_all(r'<option value="product.php\?brand=([0-9]+)">.*?</option>', index_table)

brands = list()
for brand_id in index_rows:
	brand_id = int(brand_id)
	if brand_id >= 0:
		brands.append(brand_id)

result = dict()
result['products'] = dict()
result['brands'] = dict()

brand_corrections = util.json_read(file_products_brand_corrections_json, "{}")
type_corrections = util.json_read(file_products_cosing_type_corrections_json, "{}")
types_unique = set()

for brand_id in brands:
	brand_html = crawler.crawl_selective(key="brand/%d" % brand_id, url=url_brand % brand_id, 
				regex=r'<td width="600">(.*?)</td>.*?<td width=100% valign="top">(.*?)resources/main_pagetop.gif')

	brand = dict()

	brand['name'] = parser.regex_find(r'<img src="images/logos[^"]+" title="([^"]+)"', brand_html, 1).strip()

	brand['website'] = parser.regex_find(r'<strong>Official Web Site:(.*?)</p>', brand_html, 1)
	brand['website'] = parser.regex_find(r'<a href="([^"]+)">', brand['website'], 1).strip()

	brand['country'] = parser.regex_find(r'<strong>Country:  </strong>.*?title="([^"]+)"', brand_html, 1).strip()
	if brand['country'] == "not available":
		brand['country'] = ""

	brand_name = brand['name']

	brand_key = parser.good_key(brand_name)
	parts = brand_key.split(" ")
	for i in range(1, len(parts)):
		maybe_brand_name = " ".join(parts[:-i])
		if maybe_brand_name in result['brands']:
			brand_name = result['brands'][maybe_brand_name]['name']

	if brand_name == brand['name']:
		result['brands'][brand_key] = brand

	if brand_name in brand_corrections:
		brand_name = brand_corrections[brand_name]
		brand['name'] = brand_name

	products_html = parser.regex_find(r'<Products>(.*?)</Products>', brand_html, 1)
	product_html_list = parser.regex_find_all(r'<Product>(.*?)</Product>', products_html)

	for product_html in product_html_list:
		product = dict()
		product["brand"] = brand_name

		product_type_and_name = parser.regex_find(r'<ProductName><a href=.*?type=([^&]*).*?>(.*?)</a></ProductName>', product_html, [1,2])
		product["type"] = product_type_and_name[0]
		if product["type"] in type_corrections:
			product["type"] = type_corrections[product["type"]]
		types_unique.add(product["type"])
		product["name"] = product_type_and_name[1]
		product["name"] = parser.strip_brand(brand_name, product["name"])
		if not product["name"]:
			product["name"] = product_type_and_name[1]

		product['description'] = parser.regex_find(r'class="Description".*?<Value>(.*?)</Value>', product_html, 1)
		if product['description'] == "n/a":
			product['description'] = ""

		product_active_ingredients = parser.regex_find(r'ActiveIngredientList.*?<Value>(.*?)</Value>', product_html, 1)
		product['key_ingredients'] = parser.strip_tags(product_active_ingredients)
		if product['key_ingredients'] == "n/a":
			product['key_ingredients'] = ""

		product_ingredients = parser.regex_find(r'"IngredientList.*?<Value>(.*?)</Value>', product_html, 1)
		product['ingredients'] = parser.fix_space(parser.regex_replace(r'<[^>]*?>', " ", product_ingredients)).strip()
		if product['ingredients'] == "n/a":
			product['ingredients'] = ""

		product['ingredients'] = parser.regex_replace_dict(ingredient_corrections, product['ingredients'])

		(size, price) = parser.regex_find(r'<PriceValue>(.*?), (.*?)<br />.*?</PriceValue>', product_html, [1, 2])
		product['price'] = price
		product['size'] = size

		key = parser.product_key(product['brand'], product['name'])
		if not product['key_ingredients'] == "" or not product['ingredients'] == "":
			result["products"][key] = product

result['types_unique'] = list(types_unique)
result['types_unique'].sort()

util.json_write(result, file_products_cosing_json)

parser.print_count(result["products"], "Products")
parser.print_count(result["brands"], "Brands")
