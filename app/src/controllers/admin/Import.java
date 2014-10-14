package src.controllers.admin;

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
		DBFormat.DBCache cache = new DBFormat.DBCache();

		Logger.debug(TAG, "Importing functions");
		for (DBFormat.FunctionObject object : input.ingredient_functions) {
			object.sanitize();
			create(object);
		}
		cache.cacheFunctions();

		Logger.debug(TAG, "Importing brands");
		cache.cacheBrands();
		for (DBFormat.BrandObject object : input.brands) {
			object.sanitize();
			create(object, cache);
		}
		cache.cacheBrands();

		Logger.debug(TAG, "Importing ingredients");
		cache.cacheIngredientNames();
		for (DBFormat.IngredientObject object : input.ingredients) {
			object.sanitize();
			create(object, cache);
		}
		cache.cacheIngredientNames();

		Logger.debug(TAG, "Looking through products");
		Set<String> allIngredients = new HashSet<>();
		Set<String> brands = new HashSet<>();
		for (DBFormat.ProductObject object : input.products) {
			object.sanitize();

			brands.add(object.brand);

			List<String> ingredients = cache.splitIngredients(object.ingredients);
			List<String> key_ingredients = cache.splitIngredients(object.key_ingredients);
			allIngredients.addAll(ingredients);
			allIngredients.addAll(key_ingredients);
		}

		brands.remove("");
		brands.remove(null);
		allIngredients.remove("");
		allIngredients.remove(null);

		//Create brands
		for (String brand : brands) {
			Brand b = Brand.byName(brand);
			if (b == null) {
				b = new Brand();
				b.setName(brand);
				b.setDescription("");
				b.save();
			}
		}
		cache.cacheBrands();

		for (String ingredient : allIngredients) {
			cache.matchIngredientName(ingredient);
		}
		Logger.debug(TAG, allIngredients.size() + " ingredients from all products");

		Logger.debug(TAG, "Importing products");
		for (DBFormat.ProductObject object : input.products) {
			create(object, cache);
		}
	}

	private static void create(DBFormat.BrandObject object, DBFormat.DBCache cache) {
		Brand result = cache.getBrand(object.name);

		if (result == null) {
			result = new Brand();
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();
	}

	private static void create(DBFormat.FunctionObject object) {
		Function result = Function.byName(object.name);

		if (result == null) {
			result = new Function();
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();
	}

	private static void create(DBFormat.IngredientObject object, DBFormat.DBCache cache) {
		Set<Function> functionList = new HashSet<>();
		for (String function : object.functions) {
			function = function.trim();
			if (function.isEmpty()) {
				continue;
			}
			Function function1 = cache.getFunction(function);
			if (function1 == null) {
				Logger.debug(TAG, "Function not found! " + function);
			}
			else {
				functionList.add(function1);
			}
		}

		Ingredient result = cache.getIngredient(object.name);

		if (result == null) {
			result = new Ingredient();
		}

		result.setName(object.name);
		result.setCas_number(object.cas_no);
		result.setDescription(object.description);
		result.setFunctions(functionList);

		result.save();

		create(result, object.name, cache);

		for (String name : object.names) {
			create(result, name, cache);
		}
	}

	private static void create(DBFormat.ProductObject object, DBFormat.DBCache cache) {
		List<IngredientName> ingredients = cache.matchAllIngredientNames(object.ingredients);
		List<IngredientName> key_ingredients = cache.matchAllIngredientNames(object.key_ingredients);

		Brand brand = cache.getBrand(object.brand);

		Product result = Product.byBrandAndName(brand, object.name);
		if (result == null) {
			result = new Product();
		}
		else {
			List<ProductIngredient> old_links = result.getIngredientLinks();

			for (ProductIngredient link : old_links) {
				link.delete();
			}
		}

		result.setName(object.name);
		result.setBrand(brand);
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

		result.save();
	}

	private static void create(Ingredient ingredient, String name, DBFormat.DBCache cache) {
		IngredientName ingredientName = cache.getIngredientName(name);
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
}
