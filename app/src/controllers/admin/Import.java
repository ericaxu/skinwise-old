package src.controllers.admin;

import src.App;
import src.models.MemCache;
import src.models.data.*;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		cache.matcher.cache(cache.ingredient_names.all());

		Set<String> allIngredients = new HashSet<>();
		Set<String> brands = new HashSet<>();
		Set<String> types = new HashSet<>();
		for (DBFormat.ProductObject object : input.products) {
			object.sanitize();

			brands.add(object.brand);
			types.add(object.type);

			List<String> ingredients = cache.matcher.splitIngredients(object.ingredients);
			allIngredients.addAll(ingredients);
			ingredients = cache.matcher.splitIngredients(object.key_ingredients);
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
		for (String ingredient : allIngredients) {
			cache.matcher.matchIngredientName(ingredient);
		}
		Logger.debug(TAG, allIngredients.size() + " ingredients from all products");

		Logger.debug(TAG, "Importing products");
		for (DBFormat.ProductObject object : input.products) {
			createProduct(object, cache);
		}

		cache.matcher.clear();
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

		createIngredientName(object.name, result, cache);

		for (String name : object.names) {
			createIngredientName(name, result, cache);
		}
	}

	private static void createIngredientName(String name, Ingredient ingredient, MemCache cache) {
		IngredientName result = cache.ingredient_names.get(name);
		if (result == null) {
			result = new IngredientName();
		}
		result.setName(name);
		Ingredient old = result.getIngredient();
		if (old != null && !old.equals(ingredient)) {
			Logger.error(TAG, "Ingredient name attached to multiple ingredients! " +
					ingredient.getName() + " | " + old.getName());
		}
		result.setIngredient(ingredient);
		result.save();

		cache.ingredient_names.update(result);
	}

	private static void createProduct(DBFormat.ProductObject object, MemCache cache) {
		List<IngredientName> ingredients = cache.matcher.matchAllIngredientNames(object.ingredients);
		List<IngredientName> key_ingredients = cache.matcher.matchAllIngredientNames(object.key_ingredients);

		Brand brand = cache.brands.get(object.brand);
		ProductType type = cache.types.get(object.type);

		Product result = cache.products.get(brand, object.name);
		if (result == null) {
			result = new Product();
		}
		else {
			List<ProductIngredient> old_links = result.getIngredientLinks();

			for (ProductIngredient link : old_links) {
				link.delete();
			}
		}

		result.setType(type);
		result.setDescription(object.description);

		List<ProductIngredient> ingredient_links = new ArrayList<>();

		for (IngredientName ingredient : ingredients) {
			ProductIngredient item = new ProductIngredient();
			item.setProduct(result);
			item.setIngredient_name(ingredient);
			item.setIs_key(false);
			ingredient_links.add(item);
		}

		for (IngredientName ingredient : key_ingredients) {
			ProductIngredient item = new ProductIngredient();
			item.setProduct(result);
			item.setIngredient_name(ingredient);
			item.setIs_key(true);
			ingredient_links.add(item);
		}

		result.setIngredientLinks(ingredient_links);

		cache.products.updateAndSave(result, brand, object.name);
	}
}
