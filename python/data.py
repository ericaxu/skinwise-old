import sys
import gc

from util import (web, db, parser, util)

file_data_json = "data/data.json.txt"
file_data_tmp_json = "data/data.temp.json.txt"
file_import_json = "data/import.json.txt"

file_ingredients_specialchem_json = "data/ingredients.specialchem.json.txt"
file_ingredients_cosdna_json = "data/ingredients.cosdna.json.txt"
file_products_paula_json = "data/products.paula.json.txt"
file_images_duckduckgo_json = "data/images.duckduckgo.json.txt"

def import_data():
	result = util.json_read(file_data_json, "{}")
	if not 'ingredients' in result:
		result['ingredients'] = dict()
	if not 'functions' in result:
		result['functions'] = dict()
	if not 'products' in result:
		result['products'] = dict()

	# specialchem
	specialchem = util.json_read(file_ingredients_specialchem_json, "{}")
	if 'ingredients' in specialchem:
		for key, ingredient in specialchem['ingredients'].items():
			if 'functions' in ingredient:
				ingredient['functions'].sort()
			if 'alias' in ingredient:
				ingredient['alias'].sort()
			result['ingredients'][key] = ingredient

	if 'functions' in specialchem:
		for key, functions in specialchem['functions'].items():
			result['functions'][key] = functions

	del specialchem

	# cosdna
	cosdna = util.json_read(file_ingredients_cosdna_json, "{}")
	if 'ingredients' in cosdna:
		for key, cosdna_ingredient in cosdna['ingredients'].items():
			# ingredients has the ingredient
			if key in result['ingredients']:
				ingredient = result['ingredients'][key]
				if 'alias' not in ingredient:
					ingredient['alias'] = list()
				if 'alias' in cosdna_ingredient:
					# Cosdna has some alias for us!
					ingredient['alias'].extend(cosdna_ingredient['alias'])
				# Cosdna has a name for us!
				ingredient['alias'].append(cosdna_ingredient['name'])
				ingredient['alias'] = util.list_unique(ingredient['alias'])
				ingredient['alias'].sort()

	del cosdna

	# paula
	paula = util.json_read(file_products_paula_json, "{}")
	if 'products' in paula:
		for key, product in paula['products'].items():
			if product['ingredients'] != "" or product['key_ingredients']!= "":
				result['products'][key] = product
 
	del paula

	# duckduckgo
	duckduckgo = util.json_read(file_images_duckduckgo_json, "{}")
	if 'images' in duckduckgo:
		for key, image in duckduckgo['images'].items():
			result['products'][key]['image'] = image['url']
 
	del duckduckgo

	return result

combined_data = import_data()

gc.collect()

util.json_write(combined_data, file_data_tmp_json)

# TODO: Diff

util.swap_files(file_data_tmp_json, file_data_json)

parser.print_count(combined_data['ingredients'], 'Ingredients')
parser.print_count(combined_data['functions'], 'Functions')
parser.print_count(combined_data['products'], "Products")

