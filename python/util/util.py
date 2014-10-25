
import json
import os
import collections

def json_write(input, file):
	"""
	encode input as json and save to file
	"""
	data = json_encode(input)
	mkdir(os.path.dirname(file))
	with open(file, 'w') as f:
		f.write(data)

def json_read(file, default):
	"""
	decode file as json, or return default if file does not exist
	"""
	data = default
	if os.path.isfile(file):
		with open(file, 'r') as f:
			data = f.read()
	return json_decode(data)

def json_encode(input):
	"""
	encode json with multiline formatting
	"""
	return json.dumps(input, indent='\t')

def json_decode(input):
	"""
	decode json
	"""
	return json.loads(input)

def mkdir(dir):
	"""
	safe mkdir
	"""
	if not os.path.exists(dir):
		os.makedirs(dir)

def list_unique(input):
	"""
	Return a list containing only unique items in the input
	Order is preserved
	"""
	return list(collections.OrderedDict.fromkeys(input))