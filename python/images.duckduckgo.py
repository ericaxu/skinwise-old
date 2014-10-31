import sys

from util import (web, db, parser, util)

db = db.DB("cache/duckduckgo.cache.db")
crawler = web.Crawler(db)

#File in
file_data_json = "data/data.json.txt"
file_images_duckduckgo_corrections_json = "data/images.duckduckgo.corrections.json.txt"
#File out
file_images_duckduckgo_json = "data/images.duckduckgo.json.txt"
#Crawled URLs
url_image_search = "https://duckduckgo.com/i.js?o=json&q=%s"

bad_urls = [
"ebaystatic.com",
"wp-content",
"blogspot"
]

data = util.json_read(file_data_json, "{}")
if 'products' not in data:
	sys.exit(0)

products = data['products']

image_corrections = util.json_read(file_images_duckduckgo_corrections_json, "{}")

result = dict()
result['images'] = dict()

for key, product in products.items():
	query = "%s %s" % (product['brand'], product['name'])
	query = web.urlencode(parser.regex_remove("[^0-9a-zA-Z ]", query))

	result_json = crawler.crawl(key="search/%s" % query, url=url_image_search % query)

	if not result_json:
		print(query)
		continue

	result_object = util.json_decode(result_json)
	results = result_object['results']

	final_image = None
	if key in image_corrections:
		image = dict()
		image['source'] = "Correction"
		image['width'] = 0
		image['height'] = 0
		image['url'] = image_corrections[key]

		final_image = image
	else:
		for img in results:
			image = dict()
			image['source'] = img['s']
			image['width'] = int(img['iw'])
			image['height'] = int(img['ih'])
			image['url'] = img['j']

			# size chech
			if image['width'] < 300 and image['height'] < 300:
				continue

			# wordpress
			good_url = True
			for bad_url in bad_urls:
				if bad_url in image['url']:
					good_url = False
			if not good_url:
				continue
 
			final_image = image
			break

	if final_image is None:
		print(query + " not found")
	else:
		result['images'][key] = final_image

util.json_write(result, file_images_duckduckgo_json)

parser.print_count(result['images'], "Images")
