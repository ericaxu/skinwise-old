import sys

from util import (web, db, parser, util)

db = db.DB("cache/birchbox.cache.db")
crawler = web.Crawler(db)

#File out
file_products_birchbox_json = "data/products.birchbox.json.txt"
#Crawled URLs
url_product_page = "https://www.birchbox.com/shop/skincare/%s"
url_product_list = "https://www.birchbox.com/shop/skincare?p=%s"

print("Searching product urls")

urls = list()
i = 1
while i:
	print ("On page %d."%i)
	product_list = list()
	# get list of products
	try:
		list_page = crawler.crawl_selective(key="list/%d" % i, url=url_product_list % i, 
			regex = r'<div class="mod bbox-mod-counter">(.*?)<div id="meta-data-stub" ')
	except: # if no more page to crawl
		break

	# Temporary
	if i > 2:
		break

	table_html = parser.regex_find(r'<!-- product_sort -->(.*?)<!-- END catalog/product/list.phtml -->', list_page, 1)


	# go through products
	result = parser.regex_find_all(r'<a class="product_name[^>]*href="http://www.birchbox.com/shop/skincare/([^"]*)"[^>]*>', list_page)
	urls.extend(result)
	i += 1

print("Fetching products")

result = dict()
result["products"] = dict()

for url in urls:
	product_info = crawler.crawl_selective(key="product/%s" % url, url=url_product_page % url, 
		regex = r'<div class="product-detail-page"(.*?)<div class="content-block shipping-note well">')
	_id = parser.strip_tags(parser.regex_find(r'data-product-id="(.*?)"', product_info, 1))
	name = parser.strip_tags(parser.regex_find(r'data-product-name="(.*?)"', product_info, 1))
	brand = parser.strip_tags(parser.regex_find(r'data-brand-name="(.*?)">', product_info, 1))
	ingredient = parser.strip_tags(parser.regex_find(r'<div class="bbox-target"><p>(.*?)</p>', product_info, 1))
	if not brand or not ingredient:
		continue
	name = parser.strip_brand(brand, name)
	description = parser.strip_tags(parser.regex_find(r'<span itemprop="description"><p>(.*?)</p>', product_info, 1))
	description += "<br>" + parser.strip_tags(parser.regex_find(r'How it Works</h4>(.*?)</p>', product_info, 1))
	description += "<br>" + parser.strip_tags(parser.regex_find(r'How to Use</h4>(.*?)</p>', product_info, 1))
	image = parser.strip_tags(parser.regex_find(r'<img itemprop="image" src="(.*?)"', product_info, 1))
	product = dict()
	# product["id"] = _id
	product["name"] = web.html_unescape(name)
	product["brand"] = web.html_unescape(brand)
	product["description"] = "" # web.html_unescape(description)
	product["ingredients"] = web.html_unescape(ingredient)
	product["image"] = image

	key = parser.product_key(product['brand'], product['name'])
	result["products"][key] = product

util.json_write(result, file_products_birchbox_json)

parser.print_count(result["products"], "Products")
