
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
	return json.dumps(input, indent='\t', sort_keys=True)

def json_decode(input):
	"""
	decode json
	"""
	return json.loads(input, object_hook=collections.OrderedDict)

def mkdir(dir):
	"""
	safe mkdir
	"""
	if not os.path.exists(dir):
		os.makedirs(dir)

def swap_files(from_file, to_file):
	"""
	delete to_file and rename from_file to to_file
	"""
	if os.path.exists(to_file):
		os.remove(to_file)
	if os.path.exists(from_file):
		os.rename(from_file, to_file)

def list_unique(input):
	"""
	Return a list containing only unique items in the input
	Order is preserved
	"""
	return list(collections.OrderedDict.fromkeys(input))

def isnumeric(input):
	try:
		float(input)
		return True
	except ValueError:
		return False