package src.controllers.admin;

import org.apache.commons.lang3.StringUtils;
import src.models.data.*;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Export {
	private static final String TAG = "Export";

	public static synchronized void exportDB(String path) throws IOException {
		DBFormat result = new DBFormat();
		DBFormat.DBCache cache = new DBFormat.DBCache();
		cache.cacheFunctions();

		Logger.debug(TAG, "Exporting functions");
		for (Function object : cache.functions) {
			result.ingredient_functions.add(export(object));
		}

		Logger.debug(TAG, "Exporting brands");
		for (Brand object : Brand.all()) {
			result.brands.add(export(object));
		}

		Logger.debug(TAG, "Exporting ingredients");
		cache.cacheIngredientNames();
		cache.cacheIngredients();
		for (Ingredient object : cache.ingredients) {
			result.ingredients.add(export(object, cache));
		}

		//Free memory
		cache = null;

		Logger.debug(TAG, "Exporting product types");
		for (ProductType object : ProductType.all()) {
			result.types.add(export(object));
		}

		Logger.debug(TAG, "Exporting products");
		for (Product object : Product.all()) {
			result.products.add(export(object));
		}

		String json = Json.serialize(result);
		Util.writeAll(path, json);
	}

	public static DBFormat.FunctionObject export(Function object) {
		DBFormat.FunctionObject result = new DBFormat.FunctionObject();
		result.name = object.getName();
		result.description = object.getDescription();
		return result;
	}

	public static DBFormat.IngredientObject export(Ingredient object, DBFormat.DBCache cache) {
		DBFormat.IngredientObject result = new DBFormat.IngredientObject();
		result.name = object.getName();
		result.cas_no = object.getCas_number();
		result.description = object.getDescription();
		result.functions.addAll(cache.getIngredientFunctions(object));
		result.names.addAll(cache.getIngredientNames(object));
		return result;
	}

	public static DBFormat.BrandObject export(Brand object) {
		DBFormat.BrandObject result = new DBFormat.BrandObject();
		result.name = object.getName();
		result.description = object.getDescription();
		return result;
	}

	public static DBFormat.ProductTypeObject export(ProductType object) {
		DBFormat.ProductTypeObject result = new DBFormat.ProductTypeObject();
		result.name = object.getName();
		result.description = object.getDescription();
		return result;
	}

	public static DBFormat.ProductObject export(Product object) {
		DBFormat.ProductObject result = new DBFormat.ProductObject();
		result.name = object.getName();
		result.brand = object.getBrandName();
		result.type = object.getTypeName();
		result.description = object.getDescription();
		List<String> key_ingredients = new ArrayList<>();
		for (IngredientName ing : object.getKey_ingredients()) {
			key_ingredients.add(ing.getName());
		}
		List<String> ingredients = new ArrayList<>();
		for (IngredientName ing : object.getIngredients()) {
			ingredients.add(ing.getName());
		}
		result.key_ingredients = StringUtils.join(key_ingredients, ',');
		result.ingredients = StringUtils.join(ingredients, ',');
		return result;
	}
}
