import sqlite3
import time
import sys
import signal

from util import gz

current_milli_time = lambda: int(round(time.time() * 1000))

class DB(object):
	"""
	The DB object manages a connection to the sqlite3 database

	It allows reading and writing, as well as compressing the data object using gzip
	"""
	def __init__(self, file):
		self.conn = sqlite3.connect(file)
		self.url_cache_table = "url_cache"
		self.pending_writes = 0
		self.max_pending_writes = 200
		self._create_tables()
		self._register_signal()

	def _register_signal(self):
		"""Registers an exit handler to commit & close so we don't lose data"""
		def signal_handler(signal, frame):
			sys.exit(0)

		signal.signal(signal.SIGINT, signal_handler)

		def sysexit():
			self.commit()
			self.close()

		import atexit
		atexit.register(sysexit)

	def close(self):
		self.conn.close()

	def commit(self):
		self.conn.commit()
		self.pending_writes = 0

	def _cursor(self):
		return self.conn.cursor()

	def _create_tables(self):
		c = self._cursor()
		c.execute('CREATE TABLE IF NOT EXISTS %s (key TEXT PRIMARY KEY, time INTEGER, data BLOB)' % self.url_cache_table)
		self.commit()

	def read_cache(self, key):
		"""Read an entry from the cache, or None if not found"""
		c = self._cursor()
		c.execute('SELECT * FROM url_cache WHERE key=?', (key,))
		result = c.fetchone()
		if result is not None:
			return gz.unzip(result[2])
		return None

	def write_cache(self, key, data):
		"""Write an entry to the cache"""
		time = current_milli_time()
		c = self._cursor()
		data = gz.zip(data)
		c.execute('INSERT INTO url_cache (key, time, data) VALUES (?,?,?)', (key, time, data))
		self.pending_writes += 1
		if self.pending_writes > self.max_pending_writes:
			self.commit()
