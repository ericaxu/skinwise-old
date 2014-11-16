import sys
import gc

from util import (web, db, parser, util)

file_data_json = "data/data.json.txt"
file_data_tmp_json = "data/data.temp.json.txt"
file_import_json = "data/import.json.txt"
file_export_json = "data/export.json.txt"

file_ingredients_specialchem_json = "data/ingredients.specialchem.json.txt"
file_ingredients_active_list_json = "data/ingredients.active.list.json.txt"
file_ingredients_benefits_mapping_json = "data/ingredients.benefits.mapping.json.txt"
file_ingredients_alias_additions_json = "data/ingredients.alias.additions.json.txt"
file_ingredients_cosdna_json = "data/ingredients.cosdna.json.txt"
file_ingredients_inci_json = "data/ingredients.inci.json.txt"
file_products_paula_json = "data/products.paula.json.txt"
file_products_birchbox_json = "data/products.birchbox.json.txt"
file_products_cosing_json = "data/products.cosing.json.txt"
file_images_duckduckgo_json = "data/images.duckduckgo.json.txt"
file_products_brand_corrections_json = "data/products.brand.corrections.json.txt"

def import_data():
	result = dict() # util.json_read(file_data_json, "{}")
	if not 'ingredients' in result:
		result['ingredients'] = dict()
	if not 'functions' in result:
		result['functions'] = dict()
	if not 'benefits' in result:
		result['benefits'] = dict()
	if not 'products' in result:
		result['products'] = dict()
	if not 'brands' in result:
		result['brands'] = dict()

	# specialchem ingredients
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

	# cosdna ingredients
	cosdna = util.json_read(file_ingredients_cosdna_json, "{}")
	if 'ingredients' in cosdna:
		for key, cosdna_ingredient in cosdna['ingredients'].items():
			if key in result['ingredients']:
				ingredient = result['ingredients'][key]
				if 'alias' not in ingredient:
					ingredient['alias'] = list()
				if 'alias' in cosdna_ingredient:
					# Cosdna has some alias for us!
					ingredient['alias'].extend(cosdna_ingredient['alias'])
				# Cosdna has a name for us!
				ingredient['display_name'] = cosdna_ingredient['name']
				ingredient['alias'].append(cosdna_ingredient['name'])
				ingredient['alias'] = util.list_unique(ingredient['alias'])
				ingredient['alias'].sort()

	del cosdna

	# inci functions
	inci = util.json_read(file_ingredients_inci_json, "{}")

	if 'functions' in inci:
		for key, functions in inci['functions'].items():
			result['functions'][key] = functions

	del inci

	# brands
	brand_corrections = util.json_read(file_products_brand_corrections_json, "{}")

	def fix_brand(product):
		# Brand correction applied
		if product['brand'].endswith('\u00ae') or product['brand'].endswith('\u2122'):
			product['brand'] = product['brand'][:-1]
		if product['brand'] in brand_corrections:
			product['brand'] = brand_corrections[product['brand']]
		brand_key = parser.good_key(product['brand'])
		result['brands'][brand_key] = {"name": product['brand']}

	# cosing products
	cosing = util.json_read(file_products_cosing_json, "{}")
	if 'products' in cosing:
		for key, product in cosing['products'].items():
			if product['ingredients'] != "" or product['key_ingredients']!= "":
				fix_brand(product)
				key = parser.product_key(product['brand'], product['name'])
				result['products'][key] = product
 
	del cosing

	# birchbox products
	birchbox = util.json_read(file_products_birchbox_json, "{}")
	if 'products' in birchbox:
		for key, product in birchbox['products'].items():
			if product['ingredients'] != "" or product['key_ingredients']!= "":
				fix_brand(product)
				key = parser.product_key(product['brand'], product['name'])
				if key in result['products']:
					print("birchbox duplicate: " + key)
				result['products'][key] = product
 
	del birchbox

	# paula products
	paula = util.json_read(file_products_paula_json, "{}")
	if 'products' in paula:
		for key, product in paula['products'].items():
			if product['ingredients'] != "" or product['key_ingredients']!= "":
				if product['brand'] == "Neutrogena Canada" and \
					parser.product_key("Neutrogena", product['name']) in paula['products']:
					continue
				fix_brand(product)
				key = parser.product_key(product['brand'], product['name'])
				if key in result['products']:
					print("paula's choice duplicate: " + key)
				result['products'][key] = product
 
	del paula

	del brand_corrections

	# duckduckgo images
	duckduckgo = util.json_read(file_images_duckduckgo_json, "{}")
	if 'images' in duckduckgo:
		for key, image in duckduckgo['images'].items():
			if key in result['products'] and 'image' not in result['products'][key]:
				result['products'][key]['image'] = image['url']
 
	del duckduckgo

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

	# ingredient benefits
	benefits = util.json_read(file_ingredients_benefits_mapping_json, "{}")
	for key, benefit in benefits.items():
		if key in result['ingredients']:
			ingredient = result['ingredients'][key]
			if 'benefits' not in ingredient:
				ingredient['benefits'] = benefit.split(',')
				for b in ingredient['benefits']:
					benefit_key = parser.good_key(b)
					if not benefit_key in result['benefits']:
						result['benefits'][benefit_key] = {'name': b}
	del benefits

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
	removed = list()
	for key, product in result['products'].items():
		if "Discontinued" in product['name']:
			product['popularity'] = -1
		if 'popularity' not in product:
			product['popularity'] = 0
		if "spf 0" in key:
			removed.append(key)

	for key in removed:
		result['products'].pop(key, None)

	# export popularity & IDs
	export = util.json_read(file_export_json, "{}")
	for type_key, export_values in export.items():
		if type_key in result:
			result_values = result[type_key]
			for key, value in export_values.items():
				if key in result_values and 'id' in value:
					result_values[key]['id'] = value['id']
	if 'products' in export:
		for key, product in export['products'].items():
			key = parser.product_key(product['brand'], product['name'])
			if key in result['products']:
				if 'popularity' in product:
					result['products'][key]['popularity'] = product['popularity']
	del export

	return result

combined_data = import_data()

gc.collect()

util.json_write(combined_data, file_data_tmp_json)

# TODO: Diff

util.swap_files(file_data_tmp_json, file_data_json)

parser.print_count(combined_data['ingredients'], 'Ingredients')
parser.print_count(combined_data['brands'], 'Brands')
parser.print_count(combined_data['functions'], 'Functions')
parser.print_count(combined_data['benefits'], 'Benefits')
parser.print_count(combined_data['products'], "Products")
