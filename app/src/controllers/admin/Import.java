package src.controllers.admin;

import src.models.data.Function;
import src.models.data.Ingredient;
import src.models.data.IngredientName;
import src.models.data.Product;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.*;

public class Import {
	private static final String TAG = "Import";

	public static synchronized void importDB(String path) throws IOException {
		String json = Util.readAll(path);
		ImportFormat input = Json.deserialize(json, ImportFormat.class);
		DBCache db = new DBCache();

		Logger.debug(TAG, "Importing functions");

		//Import functions
		for (IngredientFunctionObject object : input.ingredient_functions) {
			object.sanitize();
			create(object);
		}
		db.cacheFunctions();

		Logger.debug(TAG, "Importing ingredients");

		//Import ingredients
		for (IngredientObject object : input.ingredients) {
			object.sanitize();
			create(object, db);
		}
		db.cacheIngredientNames();

		Logger.debug(TAG, "Importing products");

		//Import products
		Set<String> allIngredients = new HashSet<>();
		for (ProductObject object : input.products) {
			object.sanitize();

			List<String> ingredients = db.splitIngredients(object.ingredients);
			List<String> key_ingredients = db.splitIngredients(object.key_ingredients);
			allIngredients.addAll(ingredients);
			allIngredients.addAll(key_ingredients);
		}

		allIngredients.remove("");
		allIngredients.remove(null);

		for (String ingredient : allIngredients) {
			db.matchIngredientName(ingredient);
		}

		Logger.debug(TAG, allIngredients.size() + " ingredients from all products");

		for (ProductObject object : input.products) {
			create(object, db);
		}
	}

	private static void create(IngredientFunctionObject object) {
		Function result = Function.byName(object.name);

		if (result == null) {
			result = new Function();
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();
	}

	private static void create(IngredientObject object, DBCache db) {
		Set<Function> functionList = new HashSet<>();
		for (String function : object.functions) {
			function = function.trim();
			if (function.isEmpty()) {
				continue;
			}
			Function function1 = db.matchFunction(function);
			if (function1 == null) {
				Logger.debug(TAG, "Function not found! " + function);
			}
			else {
				functionList.add(function1);
			}
		}

		Ingredient result = Ingredient.byINCIName(object.name);

		if (result == null) {
			result = new Ingredient();
		}

		result.setName(object.name);
		result.setCas_number(object.cas_no);
		result.setDescription(object.description);
		result.setFunctions(functionList);

		result.save();

		create(result, object.name);
/*
		for (String name : object.names) {
			create(result, name);
		}
*/
	}

	private static void create(ProductObject object, DBCache db) {
		List<IngredientName> ingredients = db.matchAllIngredientNames(object.ingredients);
		List<IngredientName> key_ingredients = db.matchAllIngredientNames(object.key_ingredients);

		Product result = Product.byBrandAndName(object.brand, object.name);
		if (result == null) {
			result = new Product();
		}
		result.setName(object.name);
		result.setBrand(object.brand);
		result.setDescription(object.claims);

		result.setIngredients(ingredients);
		result.setKey_ingredients(key_ingredients);

		result.save();
	}

	private static void create(Ingredient ingredient, String name) {
		IngredientName ingredientName = IngredientName.byName(name);
		if (ingredientName == null) {
			ingredientName = new IngredientName();
		}
		ingredientName.setName(name);
		Ingredient old = ingredientName.getIngredient();
		if (old != null && !old.equals(ingredient)) {
			Logger.error(TAG, "Ingredient name attached to multiple ingredients! " +
					ingredient.getName() + " | " + old.getName());
		}
		ingredientName.setIngredient(ingredient);
		ingredientName.save();
	}

	private static class DBCache {
		Map<IngredientName, Set<String>> ingredientNameWords = new HashMap<>();
		Map<String, IngredientName> ingredientNameCache = new HashMap<>();
		Map<String, Function> functionCache = new HashMap<>();

		public void cacheFunctions() {
			functionCache.clear();
			List<Function> functions = Function.all();
			for (Function function : functions) {
				functionCache.put(function.getName(), function);
			}
		}

		public void cacheIngredientNames() {
			List<IngredientName> names = IngredientName.all();
			for (IngredientName name : names) {
				cacheIngredientName(name);
			}
		}

		private void cacheIngredientName(IngredientName name) {
			ingredientNameCache.put(name.getName(), name);
			String[] words = name.getName().split("[^a-zA-Z0-9]");
			Set<String> set = new HashSet<>(Arrays.asList(words));
			set.remove("");
			ingredientNameWords.put(name, set);
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
			if (ingredientNameCache.containsKey(input)) {
				return ingredientNameCache.get(input);
			}

			String[] words = input.split("[^a-zA-Z0-9]");
			IngredientName name = null;

			for (Map.Entry<IngredientName, Set<String>> entry : ingredientNameWords.entrySet()) {
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

		public Function matchFunction(String input) {
			if (functionCache.containsKey(input)) {
				return functionCache.get(input);
			}
			return Function.byName(input.toLowerCase());
		}
	}

	public static class ImportFormat {
		public IngredientFunctionObject[] ingredient_functions;
		public IngredientObject[] ingredients;
		public ProductObject[] products;

		//Currently unused
		public IngredientAbbreviationObject[] ingredient_abbreviations;
	}

	public static class IngredientObject {
		public String name;
		public String cas_no;
		public String ec_no;
		public String description;
		public String restriction;
		public Set<String> functions;
		public Set<String> names;

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

	public static class IngredientFunctionObject {
		public String name;
		public String description;

		public void sanitize() {
			name = Util.notNull(name).trim().toLowerCase();
			description = Util.notNull(description).trim();
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

	public static class ProductObject {
		public String name;
		public String brand;
		public String claims;
		public String key_ingredients;
		public String ingredients;

		public void sanitize() {
			name = Util.notNull(name).trim();
			brand = Util.notNull(brand).trim();
			claims = Util.notNull(claims).trim();
			ingredients = Util.notNull(ingredients).trim();
			key_ingredients = Util.notNull(key_ingredients).trim();
		}
	}
}
