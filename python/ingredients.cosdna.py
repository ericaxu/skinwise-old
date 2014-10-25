import sys

from util import (web, db, parser, util)

db = db.DB("cache/cosdna.cache.db")
crawler = web.Crawler(db)

#File in
file_data_json = "data/data.json.txt"
#File out
file_ingredients_cosdna_json = "data/ingredients.cosdna.json.txt"
#Crawled URLs
url_ingredient_search = "http://cosdna.com/eng/stuff.php?q=%s"
url_ingredient_page = "http://cosdna.com/eng/%s.html"

print("Searching ingredient urls")

data = util.json_read(file_data_json, "{}")

if not data['ingredients']:
	sys.exit(0)

mapping = dict()

for key, ingredient in data['ingredients'].items():
	name = ingredient['name']
	encoded_name = web.urlencode(name.upper())
	page_html = crawler.crawl_selective(key="search/%s" % encoded_name, url=url_ingredient_search % encoded_name, \
		regex=r'<span style="float: left;">(.*?)<!-- AD -->')

	# Detect if we got redirected automatically
	result = parser.regex_find(r'<a href="\/cht\/+([a-zA-Z0-9]*)\.html">', page_html)

	if result is None:
		table_html = parser.regex_find(r'StuffResult(.*?)<\/div>', page_html, 1)
		row_html = parser.regex_find_all(r'<tr valign="top">(.*?)<\/tr>', table_html)
		if len(row_html) != 0:
			first_row_cells = parser.regex_find_all(r'<td[^>]*>(.*?)<\/td>', row_html[0])
			for cell in first_row_cells:
				result = parser.regex_find(r'href="([^"]*)\.html"', cell)
				if result:
					break

	if result is not None:
		mapping[key] = result.group(1)
	else:
		print("Cosdna ingredient not found " + name)

print("Fetching ingredients")

result = dict()
result['ingredients'] = dict()

for key, cosdna_id in mapping.items():
	page_html = crawler.crawl_selective(key="ingredient/%s" % cosdna_id, url=url_ingredient_page % cosdna_id, \
		regex=r'<!---- content start ----->(.*?)<!-- AD -->')
	ingredient_html = parser.regex_find(r'StuffDetail"(.*?)Stuff_DetailCR', page_html, 1)
	ingredient_function = parser.regex_find_all(r'<br>([^<]*?)<br>', page_html)
	ingredient_details = parser.regex_find_all(r'<div class="(.*?)">(.*?)<\/div>', ingredient_html)
	ingredient_data = parser.array_rotate(ingredient_details, 0, 1)
	ingredient = dict()
	ingredient['cosdna_id'] = cosdna_id
	ingredient['name'] = ingredient_data['Stuff_DetailE']
	if 'Stuff_DetailK' in ingredient_data:
		ingredient_alias = parser.regex_find(r'^\((.*)\)$', ingredient_data['Stuff_DetailK'], 1)
		ingredient_alias = ingredient_alias.split(',')
		# This prevents splitting things like 1,2-xsomethingnol
		tmp = list()
		cnt = len(ingredient_alias)
		for i in range(0, cnt):
			alias = ingredient_alias[i].strip()
			if len(alias) == 1 and alias.isdigit() and i < cnt-1:
				tmp.append(alias + "." + ingredient_alias[i + 1])
				i += 1
			else:
				tmp.append(alias)
		ingredient['alias'] = tmp
	
	if len(ingredient_function) > 0:
		ingredient['function'] = ingredient_function[0]
		if len(ingredient_function) > 1:
			ingredient_function.pop(0)
			ingredient['cosdna_info'] = ",".join(ingredient_function)

	if 'Stuff_Infotag' in ingredient_data:
		ingredient["cosdna_infotag"] = ingredient_data['Stuff_Infotag'].replace("：", ": ")
	if 'Stuff_DetailR' in ingredient_data:
		ingredient_related = parser.regex_find_all(r'<a href="([a-zA-Z0-9]*).html">(.*?)<\/a>', ingredient_data['Stuff_DetailR'])
		# ingredient["cosdna_related"] = dict()
		
		# for ($i=0; $i < count($ingredient_related[0]); $i++) {
		# 	$ingredient_related_id = $ingredient_related[1][$i];
		# 	$ingredient_related_name = $ingredient_related[2][$i];
		# 	$ingredient["cosdna_related"][$ingredient_related_id] = $ingredient_related_name;
		# 	if(!$ingredients[$ingredient_related_id] && !$related[$ingredient_related_id]) {
		# 		$related_search[] = $ingredient_related_id;
		# 	}
		# }

	result['ingredients'][key] = ingredient


util.json_write(result, file_ingredients_cosdna_json)

parser.print_count(result['ingredients'], "Ingredients")
