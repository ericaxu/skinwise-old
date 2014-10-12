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

		//Import functions
		for (IngredientFunctionObject object : input.ingredient_functions) {
			create(object);
		}

		//Import ingredients
		for (IngredientObject object : input.ingredients) {
			create(object);
		}

		//Import products
		IngredientMatcher matcher = new IngredientMatcher();
		for (ProductObject object : input.products) {
			create(object, matcher);
		}
	}

	private static void create(ProductObject object, IngredientMatcher matcher) {
		object.sanitize();

		List<IngredientName> ingredients = matcher.matchAll(object.ingredients);
		List<IngredientName> key_ingredients = matcher.matchAll(object.key_ingredients);

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

	private static void create(IngredientFunctionObject object) {
		object.sanitize();

		Function result = Function.byName(object.name);

		if (result == null) {
			result = new Function();
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();
	}

	private static void create(IngredientObject object) {
		object.sanitize();

		Set<Function> functionList = new HashSet<>();
		for (String function : object.functions) {
			function = function.trim();
			if (function.isEmpty()) {
				continue;
			}
			Function function1 = Function.byName(function.toLowerCase());
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

		for (String name : object.names) {
			create(result, name);
		}

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

	private static class IngredientMatcher {
		Map<IngredientName, Set<String>> map = new HashMap<>();

		public IngredientMatcher() {
			List<IngredientName> names = IngredientName.all();
			for (IngredientName name : names) {
				addIngredientName(name);
			}
		}

		private void addIngredientName(IngredientName name) {
			String[] words = name.getName().split("[^a-zA-Z0-9]");
			Set<String> set = new HashSet<>(Arrays.asList(words));
			set.remove("");
			map.put(name, set);
		}

		private List<String> splitIngredients(String ingredient_string) {
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

		public List<IngredientName> matchAll(String input) {
			List<IngredientName> matches = new ArrayList<>();
			Set<IngredientName> matchSet = new HashSet<>();
			List<String> ingredients = splitIngredients(input);
			for (String ingredient : ingredients) {
				IngredientName name = match(ingredient);
				if (!matchSet.contains(name)) {
					matches.add(name);
					matchSet.add(name);
				}
			}
			return matches;
		}

		public IngredientName match(String input) {
			String[] words = input.split("[^a-zA-Z0-9]");
			IngredientName name = null;

			for (Map.Entry<IngredientName, Set<String>> entry : map.entrySet()) {
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
			addIngredientName(name);
			return name;
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
