import sys

from util import (web, db, parser, util)

db = db.DB("cache/specialchem.cache.db")
crawler = web.Crawler(db)

#File out
file_ingredients_specialchem_json = "data/ingredients.specialchem.json.txt"
#Crawled URLs
url_search = "http://www.specialchem4cosmetics.com/services/inci/index.aspx?p=%d"
url_ingredient = "http://www.specialchem4cosmetics.com/services/inci/ingredient.aspx?id=%s"

print("Searching ingredient IDs")

search_ids = list()
for i in range(1, 647):
	table_html = crawler.crawl_selective(key="search/%d" % i, url=url_search % i, regex=r'<!-- RESULTS LIST -->(.*?)<\/table>')

	id_list = parser.regex_find_all(r'<a href="ingredient.aspx\?id=([0-9]*)"', table_html)

	for id in id_list:
		search_ids.append(id)

db.commit()

print("Parsing ingredients")

result = dict()
result['ingredients'] = dict()
result['functions'] = dict()

functions = set()

popularity = len(search_ids)
for id in search_ids:
	page_html = crawler.crawl_selective(key="ingredient/%s" % id, url=url_ingredient % id, \
		regex=r'<h2 id="inciresulth2">INCI Directory<\/h2>(.*?)<td rowspan="2" id="PageMiddleRight">')

	table_html = parser.regex_find(r'inciingredienttable(.*?)<\/table>', page_html).group(1)
	ingredient_data = parser.regex_find_all(r'<tr>(.*?)<\/tr>', table_html)

	ingredient_name = parser.strip_tags(ingredient_data[0])

	ingredient = dict()
	ingredient['name'] = ingredient_name

	ingredient_data = parser.regex_find_all(r'<td id="([^"]*)"[^>]*>(.*?)<\/td>', table_html)
	ingredient_data = parser.array_rotate(ingredient_data, 0, 1)

	ingredient['cas_no'] = parser.strip_tags(ingredient_data['inci_CASNumber'])
	ingredient['ec_no'] = parser.strip_tags(ingredient_data['inci_EINECS_ELINCS'])
	ingredient['description'] = parser.strip_tags(ingredient_data['inci_Description'])

	ingredient_restriction = parser.strip_tags(ingredient_data['inci_Restriction'])
	ingredient_restriction = parser.regex_remove(r'Last update on [0-9]+ [a-zA-Z]+ [0-9]+ - ', ingredient_restriction)
	ingredient['restriction'] = parser.regex_remove(r'no restriction', ingredient_restriction)
	
	ingredient['functions'] = parser.regex_find_all(r'<a[^>]*>(.*?)<\/a>', ingredient_data['inci_Functions'])
	for function in ingredient['functions']:
		functions.add(function.strip())

	ingredient['popularity'] = popularity

	result['ingredients'][parser.good_key(ingredient_name)] = ingredient
	popularity -= 1

for function_name in functions:
	result['functions'][parser.good_key(function_name)] = {'name': function_name}

util.json_write(result, file_ingredients_specialchem_json)

parser.print_count(result['ingredients'], 'Ingredients')
parser.print_count(result['functions'], 'Functions')
