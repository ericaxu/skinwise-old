import re

from math import log10, floor
from util import (util)

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

def strip_array(input):
	"""strip strings in array"""
	return [x.strip() for x in input]

def fix_space(input):
	"""Fix multiple spaces"""
	return regex_replace(r'\s+', " ", input)

def regex_remove(regex, input):
	"""remove substrings matching regex"""
	return regex_replace(regex, '', input)

def regex_replace(regex, replacement, input):
	"""replace substrings matching regex"""
	return get_regex(regex).sub(replacement, input)

def regex_split(regex, input):
	"""split a string by regex"""
	return get_regex(regex).split(input)

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
		return [result.group(i) for i in group]
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

def strip_brand(brand, name):
	"""strip the brand name off a product name"""
	brand_split = regex_split(r'(\W)', brand)
	name_split = regex_split(r'(\W)', name)
	first = 0
	found = False
	last = min(len(brand_split), len(name_split))
	for i in range(0, last):
		first = i
		if not brand_split[i].lower() == name_split[i].lower():
			found = True
			break
	if not found:
		first = last
	return "".join(name_split[first:]).strip()

def strip_brand_entire(brand, name):
	"""strip the brand name off a product name"""
	if name.lower().startswith(brand.lower()):
		return name[len(brand):].strip()
	return name

def print_count(list, title):
	"""print the length of the list as "Title: count" """
	print('%s: %d' % (title, len(list)))

def str_capitalize(input):
	"""Capitalize every word in the input"""
	return " ".join(word.capitalize() for word in input.split())

# This is mapped to the same method in Java
def good_key(name):
	"""Make a uniquely identifiable key for things like ingredients"""
	name = regex_replace(r'[^0-9a-zA-Z ]', ' ', name).strip().lower()
	return regex_replace(r'\s+', ' ', name)

# This is mapped to the same method in Java
def product_key(brand, name):
	"""Make a uniquely identifiable key for products"""
	return good_key("%s %s" % (brand, name))

def split_size_unit(input):
	find = regex_find(r'^([0-9\.]+)\s*(.*)', input)
	if not find:
		return (None, None)

	size = find.group(1)
	unit = find.group(2)
	if not util.isnumeric(size):
		return (None, None)
	return (size, unit)

def try_convert_unit(size, unit):
	size = float(size)
	if unit == "fl. oz.":
		size *= 29.5735
		sigfig = 2 if size > 100 else 1
		size = round(size, sigfig-1-int(floor(log10(size))))
		unit = "ml"
	size = str(size)
	if '.' in size:
		size = size.strip('0').strip('.')
	return (size, unit)

def parse_ingredients(ingredients):
	ingredients = regex_replace(r'\s*(?i)Ingredients*:', ":", ingredients).strip()

	if ingredients.startswith(":"):
		ingredients = ingredients[1:]
	key = ""
	other = ""

	ingredients = fix_space(strip_tags(ingredients))

	other = regex_find(r'(?i)(Other|Inactive) *:', ingredients)
	if other is None:
		other = regex_remove(r'^(?i)Actives* *:', ingredients).strip()
	else:
		split = regex_split(r'(?i)(Other|Inactive) *:', ingredients)
		key = regex_remove(r'^(?i)Actives* *:', split[0]).strip()
		other = split[-1].strip()

	return (key, other)
