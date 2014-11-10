import sys

from util import (web, db, parser, util)

db = db.DB("cache/birchbox.cache.db")
crawler = web.Crawler(db)

#File in
file_products_birchbox_type_corrections_json = "data/products.birchbox.type.corrections.json.txt"
#File out
file_products_birchbox_json = "data/products.birchbox.json.txt"
#Crawled URLs
url_product_page = "https://www.birchbox.com/shop/%s"
url_product_list = "https://www.birchbox.com/shop/skincare?p=%s"

dot_ingredient_lists = [
	"Nocturnal Rescue Mask",
	"Daily Dose Foaming Cleanser",
	"Glamour Shot Eyes"
]

badnames = [
	"kit",
	"set",
	"collection",
	"system",
	"duo",
	"trio",
	"microdelivery peel",
	"Travel Tote"
]

size_map = {
	"oz": "fl. oz.",
	"oz.": "fl. oz.",
	"fl oz": "fl. oz.",
	"fl. oz": "fl. oz.",
	"fl.oz": "fl. oz.",
	"fl oz.": "fl. oz.",
	"oz. each": "fl. oz.",
	"oz bottle": "fl. oz.",
	"oz (113 g)": "fl. oz.",
	"US fl oz": "fl. oz.",
	"US fl. oz.": "fl. oz.",
	"Fl Oz": "fl. oz.",
	"-oz. Cleanser": "fl. oz.",
	"fl. oz. per face mask": "fl. oz.",
	"g": "g.",
	"g Net wt": "g.",
	"g Net wt.": "g.",
	"ml": "ml.",
	"mL": "ml.",
	"ml. each": "ml.",
	"mask": "masks",
	"individual masks": "masks",
	"single-use sheet masks": "masks",
	"Sheet": "sheets",
	"Swabs": "swabs",
	"wipes inside a mirrored compact": "wipes",
	"single-use 2-step patch": "patches",
	"single-use 2-step patches": "patches",
	"single-use cloths": "cloths",
	"Individually Wrapped Packets": "packets",
	"single-use packets": "packets",
	"packets per box": "packets",
	"pk": "packets",
	"Towelettes": "towelettes"
}

print("Searching product urls")

urls = list()
i = 1
while i <= 85:
	# print ("On page %d."%i)
	product_list = list()
	# get list of products
	list_page = crawler.crawl_selective(key="list/%d" % i, url=url_product_list % i, 
			regex=r'<div class="mod bbox-mod-counter">(.*?)<div id="meta-data-stub" ')

	table_html = parser.regex_find(r'<!-- product_sort -->(.*?)<!-- END catalog/product/list.phtml -->', list_page, 1)

	# go through products
	result = parser.regex_find_all(r'<a class="product_name[^>]*href="http://www.birchbox.com/shop/([^"]*)"[^>]*>', list_page)
	if not result: 
		break
	urls.extend(result)
	i += 1

print("Fetching products")

result = dict()
result["products"] = dict()


def getIngredients(ingredients):
	ingredients = parser.regex_replace(r' (?i)Ingredients*:', ":", ingredients).strip()

	if ingredients.startswith(":"):
		ingredients = ingredients[1:]

	key = ""
	other = ""

	ingredients = parser.fix_space(ingredients)

	other = parser.regex_find(r'(?i)(Other|Inactive) *:', ingredients)
	if other is None:
		other = parser.regex_remove(r'^(?i)Actives* *:', ingredients).strip()
	else:
		split = parser.regex_split(r'(?i)(Other|Inactive) *:', ingredients)
		key = parser.regex_remove(r'^(?i)Actives* *:', split[0]).strip()
		other = split[-1].strip()

	return (key, other)

type_corrections = util.json_read(file_products_birchbox_type_corrections_json, "{}")

types = set()
types_unique = set()
unit_unique = set()

for url in urls:
	product_info = crawler.crawl_selective(key="product/%s" % url, url=url_product_page % url, 
		regex = r'<script>(bbui\.context\.set[^<]+)</script>.*<div class="product-detail-page"(.*?)<div class="content-block shipping-note well">')
	name = parser.regex_find(r'data-product-name="(.*?)"', product_info, 1).strip()
	brand = parser.regex_find(r'data-brand-name="(.*?)">', product_info, 1).strip()
	ingredients = parser.regex_replace(r'<[^>]*?>', " | ", parser.regex_find(r'<div class="bbox-target">(.*?)</div>', product_info, 1))
	ingredients = parser.regex_replace(r'\|\s*\|', "|", ingredients)
	ingredients = ingredients.strip("| ")
	ingredients = ingredients.replace(";", ",")
	if not brand or not ingredients:
		continue

	name_is_bad = False
	for bad_name in badnames:
		if parser.regex_find(r' (?i)' + bad_name, name):
			name_is_bad = True
	if name_is_bad:
		continue
	name = parser.strip_brand(brand, name)
	if name in dot_ingredient_lists:
		ingredients = ingredients.replace(".", ",")
	prod_type = parser.regex_find(r'"urls":(\[[^\]]*\])}', product_info, 1)
	prod_type = util.json_decode(prod_type)
	prod_type = [x for x in prod_type if x.startswith("skincare") and not x == "skincare/cleanser"
	 and not x == "skincare/moisturizer"]

	for x in prod_type.copy():
		parts = x.split('/')
		for i in range(0, len(parts)):
			t = "/".join(parts[:i])
			prod_type = [y for y in prod_type if y != t]

	prod_type_tmp = [x for x in prod_type if not x.startswith("skincare/holiday")]
	if len(prod_type_tmp) > 0:
		prod_type = prod_type_tmp
	else:
		print("Product only has holiday as type: " + name)

	prod_type = [x if x not in type_corrections else type_corrections[x] for x in prod_type]
	prod_type = [x for x in prod_type if x != ""]
	prod_type = list(set(prod_type))
	prod_type.sort()

	for x in prod_type:
		types_unique.add(x)
	prod_type = ",".join(prod_type)
	types.add(prod_type)

	price = parser.regex_find(r'<span itemprop="price"><strong>([^<]*)</strong></span>', product_info, 1)
	sizes = parser.regex_find(r'<label>Size:</label><span>([^<]*)</span>', product_info, 1)
	sizes = parser.regex_remove(r'&nbsp;', sizes).strip()
	# description = parser.strip_tags(parser.regex_find(r'<span itemprop="description"><p>(.*?)</p>', product_info, 1))
	# description += "<br>" + parser.strip_tags(parser.regex_find(r'How it Works</h4>(.*?)</p>', product_info, 1))
	# description += "<br>" + parser.strip_tags(parser.regex_find(r'How to Use</h4>(.*?)</p>', product_info, 1))
	sizes = [x.strip() for x in parser.regex_split(r'/|,| or|;', sizes)]
	sizes = [x for x in sizes if x]
	# print("%r"%size)
	size_tmp = list()
	for s in sizes:
		(size, unit) = parser.split_size_unit(s)
		if not size:
			print(s)
			continue
		if unit in size_map:
			unit = size_map[unit]

		unit_unique.add(unit)

		if unit == "fl. oz.":
			size_tmp = [size + " " + unit]
			break
		size_tmp.append(size + " " + unit)
	sizes = size_tmp
	image = parser.strip_tags(parser.regex_find(r'<img itemprop="image" src="(.*?)"', product_info, 1))
	product = dict()
	product["name"] = web.html_unescape(name)
	product["brand"] = web.html_unescape(brand)
	product['types'] = prod_type
	product["description"] = "" # web.html_unescape(description)
	product['price'] = price
	product['size'] = web.html_unescape(sizes[0] if len(sizes) else "")
	(key_ingredients, other_ingredients) = getIngredients(web.html_unescape(ingredients))
	product["key_ingredients"] = key_ingredients
	product["ingredients"] = other_ingredients
	product["image"] = image

	key = parser.product_key(product['brand'], product['name'])
	result["products"][key] = product

unit_unique = list(unit_unique)
unit_unique.sort()
print(unit_unique)

result['types'] = list(types)
result['types'].sort()
result['types_unique'] = list(types_unique)
result['types_unique'].sort()

util.json_write(result, file_products_birchbox_json)

parser.print_count(result["products"], "Products")
