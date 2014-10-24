
import json
import os
import collections

def json_write(input, file):
	data = json_encode(input)
	mkdir(os.path.dirname(file))
	with open(file, 'w') as f:
		f.write(data)

def json_read(file, default):
	data = default
	if os.path.isfile(file):
		with open(file, 'r') as f:
			data = f.read()
	return json_decode(data)

def json_encode(input):
	return json.dumps(input, indent='\t')

def json_decode(input):
	return json.loads(input)

def mkdir(dir):
	if not os.path.exists(dir):
		os.makedirs(dir)

def list_unique(input):
	return list(collections.OrderedDict.fromkeys(input))