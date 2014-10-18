package src.controllers.admin;

import src.models.data.*;
import src.util.Util;

import java.util.*;

public class DBFormat {
	public List<IngredientObject> ingredients = new ArrayList<>();
	public List<FunctionObject> ingredient_functions = new ArrayList<>();
	public List<BrandObject> brands = new ArrayList<>();
	public List<ProductTypeObject> types = new ArrayList<>();
	public List<ProductObject> products = new ArrayList<>();

	//Currently unused
	public List<IngredientAbbreviationObject> ingredient_abbreviations = new ArrayList<>();

	public static class FunctionObject {
		public String name;
		public String description;

		public void sanitize() {
			name = Util.notNull(name).trim().toLowerCase();
			description = Util.notNull(description).trim();
		}
	}

	public static class IngredientObject {
		public String name;
		public String cas_no;
		public String ec_no;
		public String description;
		public String restriction;
		public Set<String> functions = new HashSet<>();
		public Set<String> names = new HashSet<>();

		public void sanitize() {
			name = Util.notNull(name).trim();
			cas_no = Util.notNull(cas_no).trim();
			ec_no = Util.notNull(ec_no).trim();
			description = Util.notNull(description).trim();
			restriction = Util.notNull(restriction).trim();
			if (functions == null) {
				functions = new HashSet<>();
			}
			if (names == null) {
				names = new HashSet<>();
			}
			functions.remove("");
			functions.remove(null);
			names.remove("");
			names.remove(null);
		}
	}

	public static class IngredientAbbreviationObject {
		public String shorthand;
		public String full;

		public void sanitize() {
			shorthand = Util.notNull(shorthand);
			full = Util.notNull(full);
		}
	}

	public static class BrandObject {
		public String name;
		public String description;

		public void sanitize() {
			name = Util.notNull(name).trim();
			description = Util.notNull(description).trim();
		}
	}

	public static class ProductTypeObject {
		public String name;
		public String description;

		public void sanitize() {
			name = Util.notNull(name).trim();
			description = Util.notNull(description).trim();
		}
	}

	public static class ProductObject {
		public String name;
		public String brand;
		public String type;
		public String description;
		public String key_ingredients;
		public String ingredients;

		public void sanitize() {
			name = Util.notNull(name).trim();
			brand = Util.notNull(brand).trim();
			type = Util.notNull(type).trim();
			description = Util.notNull(description).trim();
			ingredients = Util.notNull(ingredients).trim();
			key_ingredients = Util.notNull(key_ingredients).trim();
		}
	}

	public static class DBCache {
		public List<Function> functions;
		public List<Brand> brands;
		public List<ProductType> types;
		public List<IngredientName> names;
		public List<Ingredient> ingredients;
		public Map<Ingredient, List<IngredientName>> ingredient_to_names = new HashMap<>();
		public Map<Ingredient, List<Function>> ingredient_to_function = new HashMap<>();
		Map<IngredientName, Set<String>> ingredient_name_word_index = new HashMap<>();
		public Map<String, Ingredient> ingredient_index = new HashMap<>();
		public Map<String, IngredientName> ingredient_name_index = new HashMap<>();
		public Map<String, Function> function_index = new HashMap<>();
		public Map<String, Brand> brand_index = new HashMap<>();
		public Map<String, ProductType> type_index = new HashMap<>();

		public void cacheFunctions() {
			functions = Function.all();
			for (Function function : functions) {
				function_index.put(function.getName().toLowerCase(), function);
			}
		}

		public void cacheBrands() {
			brands = Brand.all();
			for (Brand brand : brands) {
				brand_index.put(brand.getName().toLowerCase(), brand);
			}
		}

		public void cacheProductTypes() {
			types = ProductType.all();
			for (ProductType type : types) {
				type_index.put(type.getName().toLowerCase(), type);
			}
		}

		public void cacheIngredientNames() {
			names = IngredientName.all();
			for (IngredientName name : names) {
				cacheIngredientName(name);
			}
		}

		private void cacheIngredientName(IngredientName name) {
			String key = name.getName().toLowerCase();
			ingredient_name_index.put(key, name);
			String[] words = key.split("[^a-zA-Z0-9]");
			Set<String> set = new HashSet<>(Arrays.asList(words));
			set.remove("");
			ingredient_name_word_index.put(name, set);
		}

		public void cacheIngredients() {
			ingredients = Ingredient.all();
			for (Ingredient ingredient : ingredients) {
				ingredient_index.put(ingredient.getName().toLowerCase(), ingredient);
			}

			for (Ingredient i : ingredients) {
				ingredient_to_function.put(i, new ArrayList<>());
				ingredient_to_names.put(i, new ArrayList<>());
			}

			for (IngredientName name : names) {
				if (name.getIngredient() != null) {
					ingredient_to_names.get(name.getIngredient()).add(name);
				}
			}

			for (Function func : functions) {
				for (Ingredient i : func.getIngredients()) {
					ingredient_to_function.get(i).add(func);
				}
			}
		}

		public List<String> getIngredientNames(Ingredient ingredient) {
			List<String> result = new ArrayList<>();
			for (IngredientName name : ingredient_to_names.get(ingredient)) {
				result.add(name.getName());
			}
			return result;
		}

		public List<String> getIngredientFunctions(Ingredient ingredient) {
			List<String> result = new ArrayList<>();
			for (Function func : ingredient_to_function.get(ingredient)) {
				result.add(func.getName());
			}
			return result;
		}

		public List<String> splitIngredients(String ingredient_string) {
			ingredient_string = ingredient_string
					.replaceAll("[0-9\\.]+\\s*%", "")
					.replaceAll("\\(\\s*\\)", "");

			String[] ingredients = ingredient_string.split(",(?=[^\\)]*(?:\\(|$))");

			List<String> result = new ArrayList<>();
			for (String ingredient : ingredients) {
				ingredient = ingredient.trim().toLowerCase();
				if (!ingredient.isEmpty()) {
					result.add(ingredient);
				}
			}

			return result;
		}

		public List<IngredientName> matchAllIngredientNames(String input) {
			List<IngredientName> matches = new ArrayList<>();
			Set<IngredientName> matchSet = new HashSet<>();
			List<String> ingredients = splitIngredients(input);
			for (String ingredient : ingredients) {
				IngredientName name = matchIngredientName(ingredient);
				if (!matchSet.contains(name)) {
					matches.add(name);
					matchSet.add(name);
				}
			}
			return matches;
		}

		public IngredientName matchIngredientName(String input) {
			String key = input.toLowerCase();
			if (ingredient_name_index.containsKey(key)) {
				return ingredient_name_index.get(key);
			}

			String[] words = key.split("[^a-zA-Z0-9]");
			IngredientName name = null;

			for (Map.Entry<IngredientName, Set<String>> entry : ingredient_name_word_index.entrySet()) {
				boolean allmatch = true;
				for (String word : words) {
					if (Objects.equals(word, "")) {
						continue;
					}
					if (!entry.getValue().contains(word)) {
						allmatch = false;
						break;
					}
				}
				if (allmatch) {
					name = entry.getKey();
					break;
				}
			}

			if (name == null) {
				name = new IngredientName();
			}
			else if (!name.getName().equalsIgnoreCase(input)) {
				Ingredient ingredient = name.getIngredient();
				name = new IngredientName();
				name.setIngredient(ingredient);
			}
			else {
				return name;
			}
			name.setName(input);
			name.save();
			cacheIngredientName(name);
			return name;
		}

		public IngredientName getIngredientName(String input) {
			String key = input.toLowerCase();
			if (ingredient_name_index.containsKey(key)) {
				return ingredient_name_index.get(key);
			}
			IngredientName ingredient = IngredientName.byName(input);
			ingredient_name_index.put(key, ingredient);
			return ingredient;
		}

		public Ingredient getIngredient(String input) {
			String key = input.toLowerCase();
			if (ingredient_index.containsKey(key)) {
				return ingredient_index.get(key);
			}
			Ingredient ingredient = Ingredient.byINCIName(input);
			ingredient_index.put(key, ingredient);
			return ingredient;
		}

		public Function getFunction(String input) {
			String key = input.toLowerCase();
			if (function_index.containsKey(key)) {
				return function_index.get(key);
			}
			Function function = Function.byName(key);
			function_index.put(key, function);
			return function;
		}

		public Brand getBrand(String input) {
			String key = input.toLowerCase();
			if (brand_index.containsKey(key)) {
				return brand_index.get(key);
			}
			Brand brand = Brand.byName(key);
			brand_index.put(key, brand);
			return brand;
		}

		public ProductType getType(String input) {
			String key = input.toLowerCase();
			if (type_index.containsKey(key)) {
				return type_index.get(key);
			}
			ProductType type = ProductType.byName(key);
			type_index.put(key, type);
			return type;
		}
	}
}
