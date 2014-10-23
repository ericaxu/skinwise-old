import re

re_compiled = dict()

def get_regex(regex):
	if regex not in re_compiled:
		re_compiled[regex] = re.compile(regex, re.S)
	return re_compiled[regex]

def strip_tags(input):
	return regex_remove(r'<[^>]*?>', input).strip()

def strip_newline(input):
	return regex_remove('[\u000A\u000B\u000C\u000D\u0085\u2028\u2029]', input).strip()

def regex_remove(regex, input):
	return regex_replace(regex, '', input)

def regex_replace(regex, replacement, input):
	return get_regex(regex).sub(replacement, input)

def regex_replace_dict(dict, input):
	for regex, replacement in dict.items():
		input = regex_replace(regex, replacement, input)
	return input

def regex_find(regex, input, group=None):
	result = get_regex(regex).search(input)
	if group is None:
		return result
	if isinstance(group, list):
		if result is None:
			return [""] * len(group)
		data = list()
		for i in group:
			data.append(result.group(i))
		return data
	if result is None:
		return ""
	return result.group(group)

def regex_find_all(regex, input):
	return get_regex(regex).findall(input)

def array_rotate(array, col_key, col_value):
	result = dict()
	for item in array:
		result[item[col_key]] = item[col_value]
	return result

def print_count(list, title):
	print('%s: %d' % (title, len(list)))