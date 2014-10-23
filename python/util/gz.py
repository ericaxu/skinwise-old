import io
import gzip

def zip(string):
	zipped = io.BytesIO()
	unzipped = gzip.GzipFile(fileobj=zipped, mode='wb')
	unzipped.write(bytes(string, 'UTF-8'))
	unzipped.close()
	zipped.seek(0)
	return zipped.read()
	
def unzip(bytes):
	zipped = io.BytesIO()
	zipped.write(bytes)
	zipped.seek(0)
	unzipped = gzip.GzipFile(fileobj=zipped, mode='rb')
	output = unzipped.read().decode('UTF-8')
	unzipped.close()
	return output
