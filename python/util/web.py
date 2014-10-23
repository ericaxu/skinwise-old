import urllib.request
import urllib.parse

from util import parser

class Crawler(object):
	def __init__(self, db):
		self.db = db

	def crawl_selective(self, key, url, regex):
		def selective_callback(data):
			data = parser.strip_newline(data)
			return parser.regex_find(regex, data, 1)

		for retry in range(0, 2):
			result = self.crawl(key, url, selective_callback)
			if result:
				return result
		return ""

	def crawl(self, key, url, callback=None):
		data = self.db.read_cache(key)
		if data is None:
			data = self._crawl(url)
			if data:
				if callback:
					data = callback(data)
				if data:
					self.db.write_cache(key, data)
		return data

	def _crawl(self, url):
		try:
			with urllib.request.urlopen(url) as f:
				return f.read().decode('UTF-8')
		except:
			return None

def urlencode(input):
	return urllib.parse.quote_plus(input)