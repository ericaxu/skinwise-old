import re

re_compiled = dict()

def get_regex(regex):
	"""get a compiled regex object from the regex registry"""
	if regex not in re_compiled:
		re_compiled[regex] = re.compile(regex, re.S)
	return re_compiled[regex]

def strip_tags(input):
	"""strip html tags (such as <a href="">)"""
	return regex_remove(r'<[^>]*?>', input).strip()

def strip_newline(input):
	"""strip all unicode newline characters"""
	return regex_remove('[\u000A\u000B\u000C\u000D\u0085\u2028\u2029]', input).strip()

def regex_remove(regex, input):
	"""remove substrings matching regex"""
	return regex_replace(regex, '', input)

def regex_replace(regex, replacement, input):
	"""replace substrings matching regex"""
	return get_regex(regex).sub(replacement, input)

def regex_replace_dict(dict, input):
	"""
	regex replacement using a dictionary
	dict should be in the format of
	{
		"regex": "replacement",
		...
	}
	"""
	for regex, replacement in dict.items():
		input = regex_replace(regex, replacement, input)
	return input

def regex_find(regex, input, group=None):
	"""
	a regex find single match, with error avoidance

	if group is not given, then this simply returns a re.Match or None

	if group is an int, then this will simply return the result of the group or ""
	notice that group 0 is the full match, and group 1-n are the actual regex group matches
	e.g. regex_find(r'x([0-9]+)x', "x12345x", 1) => "12345"
	e.g. regex_find(r'x([0-9]+)x', "12345x", 1) => ""

	if group is a list, then this will return a list of regex match groups, or a list of ""
	e.g. regex_find(r'([0-9]+)([a-z]+)', "12345what", [0, 1]) => ["12345", "what"]
	e.g. regex_find(r'([0-9]+)([a-z]+)', "nomatch", [0, 1]) => ["", ""]
	"""
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
	"""
	a regex find all matches

	returns an empty list if no match
	if regex has 0 or 1 group, then return a list of str matching to all or the group
	if reges has more than 1 group, then return a list of tuples
	[
		("group1", "group2", ...),
		...
	]
	"""
	return get_regex(regex).findall(input)

def array_rotate(array, col_key, col_value):
	"""
	Given an 2d-array
	[
		["row1_col1", "row1_col2" ...], # row 1
		["row2_col1", "row2_col2" ...], # row 2
		...
	]
	col_key and col_value integers

	return a dict mapping keys from column col_key and values from column col_value
	{
		"row1_col1": "row1_col2",
		"row2_col1": "row2_col2",
		...
	} 
	"""
	result = dict()
	for item in array:
		result[item[col_key]] = item[col_value]
	return result

def strip_brand(brand_name, prod_name):
	prod_name_lowCase = prod_name.lower()
	for i, c in enumerate(brand_name.lower()+"  "):
		if c != prod_name_lowCase[i]:
			break
	return prod_name[i:]

def print_count(list, title):
	"""
	print the length of the list as "Title: count"
	"""
	print('%s: %d' % (title, len(list)))