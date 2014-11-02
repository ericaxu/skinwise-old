import sys

from util import (web, db, parser, util)

db = db.DB("cache/inci.cache.db")
crawler = web.Crawler(db)

#Documentation: http://eur-lex.europa.eu/search.html?qid=1412217293942&text=32006D0257&scope=EURLEX&type=quick&lang=en

#File out
file_ingredients_inci_json = "data/ingredients.inci.json.txt"
#Crawled URLs
url_data = "http://eur-lex.europa.eu/legal-content/EN/TXT/HTML/?uri=CELEX:32006D0257&rid=1"

functions_correction = {
	"Sufactant": "Surfactant",
	"Skin protectant": "Skin protecting",
	"Skin conditioning&rsquo;": "Skin conditioning",
	"viscosity/controlling": "viscosity controlling",
	"foaming cleansing": "foaming, cleansing",
	"foaming cleansing": "foaming, cleansing",
	"Hair waving": "hair waving or straightening",
	"hair waving or straightening or straightening": "hair waving or straightening"
}

print("Fetching page")

index_html = crawler.crawl(key='data', url=url_data)
index_html = parser.strip_newline(index_html)

print("Parsing ingredients")

index_table = parser.regex_find("Increases or decreases the viscosity of cosmetics(.*)The part entitled Nomenclature Conventions", 
	index_html, 1)
index_rows = parser.regex_find_all(r'<tr class="table">(.*?)<\/tr>', index_table)

result = dict()
result['ingredients'] = dict()
result['functions'] = dict()
result['abbreviations'] = dict()

for index_row in index_rows:
	index_row = parser.regex_remove(r'&nbsp;', index_row)
	index_cells = parser.regex_find_all(r'<td [^>]*>(.*?)<\/td>', index_row)
	index_cells = [parser.strip_tags(index_cell) for index_cell in index_cells]

	# Skip header
	if index_cells == "INCI name":
		continue

	ingredient = dict()
	ingredient['name'] = index_cells[0]
	#ingredient['inn_name'] = index_cells[1]
	#ingredient['ph_eur_name'] = index_cells[2]
	ingredient['cas_no'] = index_cells[3]
	ingredient['ec_no'] = index_cells[4]
	#ingredient['iupac_name'] = index_cells[5]
	ingredient['restriction'] = index_cells[6]
	ingredient_functions = parser.regex_replace_dict(functions_correction, index_cells[7])

	ingredient['functions'] = parser.strip_array(ingredient_functions.split(","))

	result['ingredients'][parser.good_key(ingredient['name'])] = ingredient

print("Parsing functions")

index_function_html = parser.regex_find(r'functions are defined as follows(.*?)INCI name', index_html, 1)
index_functions = parser.regex_find_all(r'<table(.*?)<\/table>', index_function_html)

for index_row in index_functions:
	function_name = parser.regex_find(r'<span class="bold">(.*?)<\/span>', index_row, 1)
	function_sections = parser.regex_find_all(r'<p class="normal">(.*?)<\/p>', index_row)

	function = dict()
	function['name'] = function_name
	function['description'] = function_sections[-1]
	result['functions'][parser.good_key(function['name'])] = function

print("Parsing abbreviations")
index_abbreviation_html = parser.regex_find(r'cosmetic ingredients in the Inventory(.*)<\/table>', index_html, 1)
index_abbreviations = parser.regex_find_all(r'<tr class="table">(.*?)<\/tr>', index_abbreviation_html)

for index_row in index_abbreviations:
	abbreviation_sections = parser.regex_find_all(r'<p class="tbl-txt">(.*?)<\/p>', index_row)

	abbreviation = dict()
	abbreviation['shorthand'] = abbreviation_sections[0]
	abbreviation['full'] = abbreviation_sections[1]
	result['abbreviations'][parser.good_key(abbreviation['full'])] = abbreviation

util.json_write(result, file_ingredients_inci_json)

parser.print_count(result['ingredients'], 'Ingredients')
parser.print_count(result['functions'], 'Functions')
parser.print_count(result['abbreviations'], 'Abbreviations')
