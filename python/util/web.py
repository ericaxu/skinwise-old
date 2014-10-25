import urllib.request
import urllib.parse
import html

from util import parser

class Crawler(object):
	"""
	A crawler that uses a sqlite3 database to cache web pages
	"""
	def __init__(self, db):
		self.db = db

	def crawl_selective(self, key, url, regex):
		"""
		Same as crawl(), except:
		regex is used to strip away useless pieces of pages by matching only the parts that we are interested in
		One example for regex is r'<!--Begin Page Content-->(.*?)<!--End Page Content-->'
		"""
		def selective_callback(data):
			data = parser.strip_newline(data)
			return parser.regex_find(regex, data, 1)

		return self.crawl(key, url, selective_callback)

	def crawl(self, key, url, callback=None):
		"""
		Crawl the page located at url
		This page is identified by key for caching purposes
		
		callback is a method (str) => (str) which will process the crawled page
		This is usually done in order to save space by stripping away useless pieces of a webpage
		"""
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
	"""Encode the input in url format (e.g. " " => "%20") """
	return urllib.parse.quote_plus(input)
	
def html_unescape(input):
	return html.unescape(input)
