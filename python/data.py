import sys
import gc

from util import (web, db, parser, util)

file_data_json = "data/data.json.txt"
file_data_tmp_json = "data/data.temp.json.txt"
file_import_json = "data/import.json.txt"
file_export_json = "data/export.json.txt"

file_ingredients_specialchem_json = "data/ingredients.specialchem.json.txt"
file_ingredients_active_list_json = "data/ingredients.active.list.json.txt"
file_ingredients_cosdna_json = "data/ingredients.cosdna.json.txt"
file_ingredients_inci_json = "data/ingredients.inci.json.txt"
file_products_paula_json = "data/products.paula.json.txt"
file_products_birchbox_json = "data/products.birchbox.json.txt"
file_products_cosing_json = "data/products.cosing.json.txt"
file_images_duckduckgo_json = "data/images.duckduckgo.json.txt"
file_ingredients_alias_additions_json = "data/ingredients.alias.additions.json.txt"
file_products_brand_corrections_json = "data/products.brand.corrections.json.txt"

def import_data():
	result = dict() # util.json_read(file_data_json, "{}")
	if not 'ingredients' in result:
		result['ingredients'] = dict()
	if not 'functions' in result:
		result['functions'] = dict()
	if not 'products' in result:
		result['products'] = dict()
	if not 'brands' in result:
		result['brands'] = dict()

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

	# inci
	inci = util.json_read(file_ingredients_inci_json, "{}")

	if 'functions' in inci:
		for key, functions in inci['functions'].items():
			result['functions'][key] = functions

	del inci

	# cosing
	cosing = util.json_read(file_products_cosing_json, "{}")
	if 'products' in cosing:
		for key, product in cosing['products'].items():
			if product['ingredients'] != "" or product['key_ingredients']!= "":
				result['products'][key] = product
 
	del cosing

	# birchbox
	birchbox = util.json_read(file_products_birchbox_json, "{}")
	if 'products' in birchbox:
		for key, product in birchbox['products'].items():
			if product['ingredients'] != "" or product['key_ingredients']!= "":
				if key in result['products']:
					print("birchbox duplicate: " + key)
				result['products'][key] = product
 
	del birchbox

	# paula
	paula = util.json_read(file_products_paula_json, "{}")
	if 'products' in paula:
		for key, product in paula['products'].items():
			if product['ingredients'] != "" or product['key_ingredients']!= "":
				if key in result['products']:
					print("paula's choice duplicate: " + key)
				result['products'][key] = product
 
	del paula

	# duckduckgo
	duckduckgo = util.json_read(file_images_duckduckgo_json, "{}")
	if 'images' in duckduckgo:
		for key, image in duckduckgo['images'].items():
			if key in result['products']:
				result['products'][key]['image'] = image['url']
 
	del duckduckgo

	# export popularity
	export = util.json_read(file_export_json, "{}")
	if 'products' in export:
		for key, product in export['products'].items():
			key = parser.product_key(product['brand'], product['name'])
			if key in result['products'] and 'popularity' in product:
				result['products'][key]['popularity'] = product['popularity']
	del export

	# ingredient aliases
	aliases = util.json_read(file_ingredients_alias_additions_json, "{}")
	for key, alias_obj in aliases.items():
		if key in result['ingredients']:
			ingredient = result['ingredients'][key]
			if 'alias' not in ingredient:
				ingredient['alias'] = list()
			for alias, status in alias_obj.items():
				if status:
					ingredient['alias'].append(alias)
				elif alias in ingredient['alias']:
					ingredient['alias'].remove(alias)
			ingredient['alias'] = util.list_unique(ingredient['alias'])
			ingredient['alias'].sort()
	del aliases

	# active ingredients
	for key, ingredient in result['ingredients'].items():
		ingredient['active'] = False

	active = util.json_read(file_ingredients_active_list_json, "{}")
	for key in active:
		if key in result['ingredients']:
			result['ingredients'][key]['active'] = True
		else:
			print (key)
	del active

	# discontinued
	for key, product in result['products'].items():
		if "Discontinued" in product['name']:
			product['popularity'] = -1
		if 'popularity' not in product:
			product['popularity'] = 0

	# brands
	corrections = util.json_read(file_products_brand_corrections_json, "{}")
	for key, product in result['products'].items():
		# Brand correction applied
		if product['brand'] in corrections:
			product['brand'] = corrections[product['brand']]
		if product['brand'].endswith('\u00ae') or product['brand'].endswith('\u2122'):
			product['brand'] = product['brand'][:-1]
		brand_key = parser.good_key(product['brand'])
		result['brands'][brand_key] = {"name": product['brand']}
	del corrections

	return result

combined_data = import_data()

gc.collect()

util.json_write(combined_data, file_data_tmp_json)

# TODO: Diff

util.swap_files(file_data_tmp_json, file_data_json)

parser.print_count(combined_data['ingredients'], 'Ingredients')
parser.print_count(combined_data['brands'], 'Brands')
parser.print_count(combined_data['functions'], 'Functions')
parser.print_count(combined_data['products'], "Products")
