package src.controllers.admin;

import org.apache.commons.lang3.StringUtils;
import src.App;
import src.models.MemCache;
import src.models.data.*;
import src.models.util.NamedModel;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Export {
	private static final String TAG = "Export";

	public static synchronized void exportDB(String path) throws IOException {
		DBFormat result = new DBFormat();
		MemCache cache = App.cache();

		Logger.debug(TAG, "Exporting functions");
		for (Function object : cache.functions.all()) {
			DBFormat.NamedObject resultObject = export(object);
			result.functions.put(Util.goodKey(resultObject.name), resultObject);
		}

		Logger.debug(TAG, "Exporting benefits");
		for (Benefit object : cache.benefits.all()) {
			DBFormat.NamedObject resultObject = export(object);
			result.benefits.put(Util.goodKey(resultObject.name), resultObject);
		}

		Logger.debug(TAG, "Exporting brands");
		for (Brand object : cache.brands.all()) {
			DBFormat.NamedObject resultObject = export(object);
			result.brands.put(Util.goodKey(resultObject.name), resultObject);
		}

		Logger.debug(TAG, "Exporting ingredients");
		for (Ingredient object : cache.ingredients.all()) {
			DBFormat.IngredientObject resultObject = export(object);
			result.ingredients.put(Util.goodKey(resultObject.name), resultObject);
		}

		Logger.debug(TAG, "Exporting product types");
		for (Type object : cache.types.all()) {
			DBFormat.TypeOject resultObject = export(object);
			result.types.put(Util.goodKey(resultObject.name), resultObject);
		}

		Logger.debug(TAG, "Exporting products");
		for (Product object : cache.products.all()) {
			DBFormat.ProductObject resultObject = export(object);
			result.products.put(Util.goodProductKey(resultObject.brand, resultObject.name), resultObject);
		}

		String json = Json.serialize(result);
		Util.writeAll(path, json);
	}

	public static <T extends DBFormat.NamedObject> T exportNamedModel(NamedModel object, T result) {
		result.id = object.getId();
		result.name = object.getName();
		result.description = object.getDescription();
		return result;
	}

	public static DBFormat.NamedObject exportNamedModel(NamedModel object) {
		return exportNamedModel(object, new DBFormat.NamedObject());
	}

	public static DBFormat.NamedObject export(Function object) {
		return exportNamedModel(object);
	}

	public static DBFormat.NamedObject export(Benefit object) {
		return exportNamedModel(object);
	}

	public static DBFormat.IngredientObject export(Ingredient object) {
		DBFormat.IngredientObject result = exportNamedModel(object, new DBFormat.IngredientObject());
		result.popularity = object.getPopularity();
		result.active = object.isActive();
		for (Function function : object.getFunctions()) {
			result.functions.add(function.getName());
		}
		for (Benefit benefit : object.getBenefits()) {
			result.benefits.add(benefit.getName());
		}
		List<Alias> aliases = App.cache().alias.getList(object.getAliases().toArray());
		for (Alias alias : aliases) {
			result.alias.add(alias.getName());
		}
		Collections.sort(result.functions);
		Collections.sort(result.benefits);
		Collections.sort(result.alias);
		return result;
	}

	public static DBFormat.NamedObject export(Brand object) {
		return exportNamedModel(object);
	}

	public static DBFormat.TypeOject export(Type object) {
		DBFormat.TypeOject result = exportNamedModel(object, new DBFormat.TypeOject());
		Type parent = object.getParent();
		if (parent != null) {
			result.parent = parent.getName();
		}
		else {
			result.parent = "";
		}
		return result;
	}

	public static DBFormat.ProductObject export(Product object) {
		DBFormat.ProductObject result = exportNamedModel(object, new DBFormat.ProductObject());
		result.brand = object.getBrandName();
		result.image = object.getImage();
		result.price = object.getFormattedPrice();
		result.size = object.getFormattedSize();
		Set<Type> types = object.getTypes();
		List<String> typesString = new ArrayList<>();
		for (Type type : types) {
			typesString.add(type.getName());
		}
		Collections.sort(typesString);
		result.types = Util.joinString(",", typesString);
		List<String> key_ingredients = new ArrayList<>();
		for (Alias ing : object.getKey_ingredients()) {
			key_ingredients.add(ing.getName());
		}
		List<String> ingredients = new ArrayList<>();
		for (Alias ing : object.getIngredients()) {
			ingredients.add(ing.getName());
		}
		result.popularity = object.getPopularity();
		result.key_ingredients = StringUtils.join(key_ingredients, ',');
		result.ingredients = StringUtils.join(ingredients, ',');
		return result;
	}
}
