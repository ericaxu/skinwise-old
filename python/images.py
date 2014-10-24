
import collections
import urllib
import html

from util import (web, db, parser, util)

db = db.DB("cache/duckduckgo.cache.db")
crawler = web.Crawler(db)

file_data_json = "data/data.json.txt"
file_products_json = "data/products.json.txt"
file_images_json = "data/images.json.txt"
url_image_search= "https://duckduckgo.com/i.js?o=json&q=%s"

data = util.json_read(file_products_json, dict())
if not data['products']:
	sys.exit(0)

products = data['products']

images = list()

for product in products:
	query = "%s %s" % (product['brand'], product['name'])
	query = web.urlencode(parser.regex_remove("[^0-9a-zA-Z ]", query))

	result_json = crawler.crawl(key="search/%s" % query, url=url_image_search % query)

	if not result_json:
		print(query)
		continue

	result_object = util.json_decode(result_json)
	results = result_object['results']

	final_image = None
	for result in results:
		image = collections.OrderedDict()
		image['name'] = product['name']
		image['brand'] = product['brand']
		image['source'] = result['s']
		image['width'] = int(result['iw'])
		image['height'] = int(result['ih'])
		image['url'] = result['j']

		# size chech
		if image['width'] < 300 and image['height'] < 300:
			continue

		final_image = image
		break

	if final_image is None:
		print(query + " not found")
	else:
		images.append(final_image)


result = collections.OrderedDict()
result['images'] = images
util.json_write(result, file_images_json)
