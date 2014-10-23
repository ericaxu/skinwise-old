
import collections

from util import (web, db, parser, util)

db = db.DB("cache/specialchem.cache.db")
crawler = web.Crawler(db)

url_search = "http://www.specialchem4cosmetics.com/services/inci/index.aspx?p=%d"
url_search = "http://127.0.0.1/cache.php?file=specialchem/search/%d.html"
url_ingredient = "http://www.specialchem4cosmetics.com/services/inci/ingredient.aspx?id=%s"
url_ingredient = "http://127.0.0.1/cache.php?file=specialchem/ingredient/%s.html"

file_data_json = "data/data.json.txt"

print("Searching ingredient IDs")

search_ids = list()
for i in range(1, 647):
	table_html = crawler.crawl_selective(key="search/%d" % i, url=url_search % i, regex=r'<!-- RESULTS LIST -->(.*?)<\/table>')

	id_list = parser.regex_find_all(r'<a href="ingredient.aspx\?id=([0-9]*)"', table_html)

	for id in id_list:
		search_ids.append(id)

db.commit()

print("Parsing ingredients")

ingredients = list()
functions = set()

for id in search_ids:
	page_html = crawler.crawl_selective(key="ingredient/%s" % id, url=url_ingredient % id, \
		regex=r'<h2 id="inciresulth2">INCI Directory<\/h2>(.*?)<td rowspan="2" id="PageMiddleRight">')

	table_html = parser.regex_find(r'inciingredienttable(.*?)<\/table>', page_html).group(1)
	ingredient_data = parser.regex_find_all(r'<tr>(.*?)<\/tr>', table_html)

	ingredient = collections.OrderedDict()
	ingredient['name'] = parser.strip_tags(ingredient_data[0])

	ingredient_data = parser.regex_find_all(r'<td id="([^"]*)"[^>]*>(.*?)<\/td>', table_html)
	ingredient_data = parser.array_rotate(ingredient_data, 0, 1)

	ingredient['cas_no'] = parser.strip_tags(ingredient_data['inci_CASNumber'])
	ingredient['ec_no'] = parser.strip_tags(ingredient_data['inci_EINECS_ELINCS'])
	ingredient['description'] = parser.strip_tags(ingredient_data['inci_Description'])

	ingredient_restriction = parser.strip_tags(ingredient_data['inci_Restriction'])
	ingredient_restriction = parser.regex_remove(r'Last update on [0-9]+ [a-zA-Z]+ [0-9]+ - ', ingredient_restriction)
	ingredient['restriction'] = parser.regex_remove(r'no restriction', ingredient_restriction);
	
	ingredient['functions'] = parser.regex_find_all(r'<a[^>]*>(.*?)<\/a>', ingredient_data['inci_Functions']);
	for function in ingredient['functions']:
		functions.add(function.strip())

	ingredients.append(ingredient)

ingredient_functions = list()
for function in functions:
	ingredient_functions.append({'name': function})


result = collections.OrderedDict();
result['ingredients'] = ingredients;
result['ingredient_functions'] = ingredient_functions;

util.json_write(result, file_data_json)

parser.print_count(ingredients, 'Ingredients');
parser.print_count(ingredient_functions, 'Functions');
