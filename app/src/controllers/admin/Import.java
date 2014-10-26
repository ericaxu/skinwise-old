package src.controllers.admin;

import src.App;
import src.models.MemCache;
import src.models.data.*;
import src.util.JoinableExecutor;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

public class Import {
	private static final String TAG = "Import";

	public static synchronized void importDB(String path) throws IOException {
		String json = Util.readAll(path);
		DBFormat input = Json.deserialize(json, DBFormat.class);
		MemCache cache = App.cache();

		Logger.debug(TAG, "Importing functions");
		for (DBFormat.NamedObject object : input.ingredient_functions) {
			object.sanitize();
			createFunction(object, cache);
		}

		Logger.debug(TAG, "Importing brands");
		for (DBFormat.NamedObject object : input.brands) {
			object.sanitize();
			createBrand(object, cache);
		}

		Logger.debug(TAG, "Importing product types");
		for (DBFormat.NamedObject object : input.types) {
			object.sanitize();
			createType(object, cache);
		}

		Logger.debug(TAG, "Importing ingredients");
		for (DBFormat.IngredientObject object : input.ingredients) {
			object.sanitize();
			createIngredient(object, cache);
		}

		Logger.debug(TAG, "Looking through products");

		Set<String> allIngredients = new HashSet<>();
		Set<String> brands = new HashSet<>();
		Set<String> types = new HashSet<>();
		for (DBFormat.ProductObject object : input.products) {
			object.sanitize();

			brands.add(object.brand);
			types.add(object.type);

			List<String> ingredients = MemCache.Matcher.splitIngredients(object.ingredients);
			allIngredients.addAll(ingredients);
			ingredients = MemCache.Matcher.splitIngredients(object.key_ingredients);
			allIngredients.addAll(ingredients);
		}

		brands.remove("");
		types.remove("");
		allIngredients.remove("");

		Logger.debug(TAG, "Importing product brands and types");
		//Create brands not entered in the system
		for (String brand : brands) {
			Brand object = cache.brands.get(brand);
			if (object == null) {
				object = new Brand();
				object.setName(brand);
				object.setDescription("");
				object.save();
				cache.brands.update(object);
			}
		}

		//Create types not entered in the system
		for (String type : types) {
			ProductType object = cache.types.get(type);
			if (object == null) {
				object = new ProductType();
				object.setName(type);
				object.setDescription("");
				object.save();
				cache.types.update(object);
			}
		}

		Logger.debug(TAG, "Importing product ingredients");

		//First pass
		Logger.debug(TAG, "Ingredient names - first pass");
		List<Alias> pending = multithreadedIngredientSearch(allIngredients, cache);

		//Spill to DB
		Logger.debug(TAG, "Ingredient names - spill");
		for (Alias name : pending) {
			name.save();
			cache.alias.update(name);
		}

		//Second pass
		Logger.debug(TAG, "Ingredient names - second pass");
		for (String string : allIngredients) {
			Alias alias = cache.matcher.matchAlias(string);

			if (alias == null) {
				alias = new Alias();
			}
			else if (!alias.getName().equalsIgnoreCase(string)) {
				alias = new Alias();
				Ingredient ingredient = alias.getIngredient();
				alias.setIngredient(ingredient);
			}
			else {
				continue;
			}

			alias.setName(string);
			alias.save();
			cache.alias.update(alias);
		}

		Logger.debug(TAG, allIngredients.size() + " ingredients from all products");

		Logger.debug(TAG, "Importing products");
		for (DBFormat.ProductObject object : input.products) {
			createProduct(object, cache);
		}
	}

	private static void createFunction(DBFormat.NamedObject object, MemCache cache) {
		Function result = cache.functions.get(object.name);

		if (result == null) {
			result = new Function();
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();

		cache.functions.update(result);
	}

	private static void createBrand(DBFormat.NamedObject object, MemCache cache) {
		Brand result = cache.brands.get(object.name);

		if (result == null) {
			result = new Brand();
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();

		cache.brands.update(result);
	}

	private static void createType(DBFormat.NamedObject object, MemCache cache) {
		ProductType result = cache.types.get(object.name);

		if (result == null) {
			result = new ProductType();
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();

		cache.types.update(result);
	}

	private static void createIngredient(DBFormat.IngredientObject object, MemCache cache) {
		//Remove duplicates
		for (String name : object.names) {
			Alias alias = cache.alias.get(name);
			if (alias == null) {
				continue;
			}
			Ingredient ingredient = alias.getIngredient();
			if (ingredient != null && !Objects.equals(ingredient.getName(), object.name)) {
				//Logger.debug(TAG, "Duplicate ingredient " +
				//		object.name + " | " + ingredient.getName());

				//Attach alt names to ingredient
				for (String name2 : object.names) {
					createAlias(name2, ingredient, cache);
				}
				//Attach info if possible
				if (ingredient.getCas_number().isEmpty()) {
					ingredient.setCas_number(object.cas_no);
				}
				if (ingredient.getDescription().isEmpty()) {
					ingredient.setDescription(object.description);
				}
				ingredient.save();
				return;
			}
		}

		Set<Function> functionList = new HashSet<>();
		for (String f : object.functions) {
			f = f.trim();
			if (f.isEmpty()) {
				continue;
			}
			Function function = cache.functions.get(f);
			if (function == null) {
				Logger.debug(TAG, "Function not found! " + f);
			}
			else {
				functionList.add(function);
			}
		}

		Ingredient result = cache.ingredients.get(object.name);

		if (result == null) {
			result = new Ingredient();
		}

		result.setName(object.name);
		result.setCas_number(object.cas_no);
		result.setDescription(object.description);
		result.setFunctions(functionList);

		result.save();

		cache.ingredients.update(result);

		createAlias(object.name, result, cache);

		for (String name : object.names) {
			createAlias(name, result, cache);
		}
	}

	private static void createAlias(String alias, Ingredient ingredient, MemCache cache) {
		Alias result = cache.alias.get(alias);
		if (result == null) {
			result = new Alias();
		}

		Ingredient old = result.getIngredient();
		if (old != null && !old.equals(ingredient)) {
			Logger.debug(TAG, alias + " attached to multiple ingredients " +
					ingredient.getName() + " | " + old.getName());
		}

		result.setName(alias);
		result.setIngredient(ingredient);
		result.save();

		cache.alias.update(result);
	}

	private static void createProduct(DBFormat.ProductObject object, MemCache cache) {
		List<Alias> ingredients = cache.matcher.matchAllAliases(object.ingredients);
		List<Alias> key_ingredients = cache.matcher.matchAllAliases(object.key_ingredients);

		Brand brand = cache.brands.get(object.brand);
		ProductType type = cache.types.get(object.type);

		Product result = cache.products.get(brand.getId(), object.name);
		if (result == null) {
			result = new Product();
		}

		String oldName = result.getName();
		long oldBrandId = result.getBrand_id();
		result.setName(object.name);
		result.setBrand(brand);
		result.setDescription(object.description);
		result.setImage(object.image);
		result.setIngredients(ingredients);
		result.setKeyIngredients(key_ingredients);

		result.save();

		cache.products.update(result, oldBrandId, oldName);

	}

	private static List<Alias> multithreadedIngredientSearch(Set<String> ingredients, MemCache cache) {
		ConcurrentMap<String, Alias> results = new ConcurrentHashMap<>();

		int threads = Runtime.getRuntime().availableProcessors() - 1;
		JoinableExecutor executor = new JoinableExecutor(Executors.newFixedThreadPool(threads));

		for (String name : ingredients) {
			executor.execute(new IngredientSearch(name, results));
		}

		try {
			executor.join();
		}
		catch (InterruptedException e) {
			Logger.error(TAG, e);
		}

		return new ArrayList<>(results.values());
	}

	private static class IngredientSearch implements Runnable {
		private String name;
		private ConcurrentMap<String, Alias> results;

		public IngredientSearch(String name, ConcurrentMap<String, Alias> results) {
			this.name = name;
			this.results = results;
		}

		@Override
		public void run() {
			Alias alias = App.cache().matcher.matchAlias(name);
			if (alias != null && !alias.getName().equalsIgnoreCase(name)) {
				Ingredient ingredient = alias.getIngredient();
				if (ingredient != null) {
					alias = new Alias();
					alias.setName(name);
					alias.setIngredient(ingredient);
					results.putIfAbsent(name, alias);
				}
			}
		}
	}
}
