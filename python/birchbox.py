import collections
import urllib
import html

from util import (web, db, parser, util)


db = db.DB("cache/birchbox.cache.db")
crawler = web.Crawler(db)

file_products_json = "data/birchbox.json.txt"
url_product_page = "https://www.birchbox.com/shop/skincare/%s"
url_product_list = "https://www.birchbox.com/shop/skincare?p=%s"

products = collections.OrderedDict()
i = 1
while(i):
	print ("On page %d."%i)
	product_list = list()
	# get list of products
	try:
		list_page = crawler.crawl_selective(key="list/%d"%i, url=url_product_list % i, 
			regex = r'<div class="mod bbox-mod-counter">(.*?)<div id="meta-data-stub" ')
	except: # if no more page to crawl
		break

	if (i>2): break

	number_displayed = list(map(int, parser.regex_find(r'<!-- Display start and end if page mode -->(.*?) of', 
						list_page, 1).split(" &ndash; ")))
	# go through products
	for j in range(1, number_displayed[1]-number_displayed[0]+2):
		product_url = parser.regex_find(r'data-pos="%d"[^>]*data-product-ids[^>]*/shop/skincare/(.*?)"' % j, 
						list_page, 1)
		product_list.append(product_url)

	for p_url in product_list:
		product_info = crawler.crawl_selective(key="product/%s"%p_url, url=url_product_page % p_url, 
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
		product = collections.OrderedDict()
		product["id"] = _id
		product["name"] = html.unescape(name)
		product["brand"] = html.unescape(brand)
		product["description"] = html.unescape(description)
		product["ingredients"] = html.unescape(ingredient)
		product["image"] = image
		products[_id] = product

	i += 1

result = collections.OrderedDict()
result["products"] = products

util.json_write(result, file_products_json)

parser.print_count(products, "Products")
