
import json
import os
import collections

def json_write(data, file):
	data = json.dumps(data, indent='\t')
	mkdir(os.path.dirname(file))
	with open(file, 'w') as f:
		f.write(data)

def json_read(file, default):
	data = default
	if os.path.isfile(file):
		with open(file, 'r') as f:
			data = f.read()
	return json.loads(data)

def mkdir(dir):
	if not os.path.exists(dir):
		os.makedirs(dir)

def list_unique(input):
	return list(collections.OrderedDict.fromkeys(input))