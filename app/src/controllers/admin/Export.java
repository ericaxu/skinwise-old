package src.controllers.admin;

import org.apache.commons.lang3.StringUtils;
import src.App;
import src.models.MemCache;
import src.models.data.*;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.*;

public class Export {
	private static final String TAG = "Export";

	public static synchronized void exportDB(String path) throws IOException {
		DBFormat result = new DBFormat();
		MemCache cache = App.cache();

		Logger.debug(TAG, "Exporting functions");
		for (Function object : cache.functions.all()) {
			result.ingredient_functions.add(export(object));
		}

		Logger.debug(TAG, "Exporting brands");
		for (Brand object : cache.brands.all()) {
			result.brands.add(export(object));
		}

		Logger.debug(TAG, "Exporting ingredients");
		for (Ingredient object : cache.ingredients.all()) {
			result.ingredients.add(export(object));
		}

		Logger.debug(TAG, "Exporting product types");
		for (ProductType object : cache.types.all()) {
			result.types.add(export(object));
		}

		Logger.debug(TAG, "Exporting products");
		for (Product object : cache.products.all()) {
			result.products.add(export(object));
		}

		String json = Json.serialize(result);
		Util.writeAll(path, json);
	}

	public static DBFormat.NamedObject export(Function object) {
		DBFormat.NamedObject result = new DBFormat.NamedObject();
		result.name = object.getName();
		result.description = object.getDescription();
		return result;
	}

	public static DBFormat.IngredientObject export(Ingredient object) {
		DBFormat.IngredientObject result = new DBFormat.IngredientObject();
		result.name = object.getName();
		result.cas_no = object.getCas_number();
		result.description = object.getDescription();
		for (Function function : object.getFunctions()) {
			result.functions.add(function.getName());
		}
		for (Alias name : object.getAliases()) {
			result.names.add(name.getName());
		}
		Collections.sort(result.functions);
		Collections.sort(result.names);
		return result;
	}

	public static DBFormat.NamedObject export(Brand object) {
		DBFormat.NamedObject result = new DBFormat.NamedObject();
		result.name = object.getName();
		result.description = object.getDescription();
		return result;
	}

	public static DBFormat.NamedObject export(ProductType object) {
		DBFormat.NamedObject result = new DBFormat.NamedObject();
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
		result.image = object.getImage();
		List<String> key_ingredients = new ArrayList<>();
		for (Alias ing : object.getKey_ingredients()) {
			key_ingredients.add(ing.getName());
		}
		List<String> ingredients = new ArrayList<>();
		for (Alias ing : object.getIngredients()) {
			ingredients.add(ing.getName());
		}
		result.key_ingredients = StringUtils.join(key_ingredients, ',');
		result.ingredients = StringUtils.join(ingredients, ',');
		return result;
	}
}
