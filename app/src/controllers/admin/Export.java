package src.controllers.admin;

import org.apache.commons.lang3.StringUtils;
import src.App;
import src.models.MemCache;
import src.models.data.*;
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
		result.popularity = object.getPopularity();
		result.active = object.isActive();
		for (Function function : object.getFunctions()) {
			result.functions.add(function.getName());
		}
		List<Alias> aliases = App.cache().alias.getList(object.getAliases().toArray());
		for (Alias alias : aliases) {
			result.alias.add(alias.getName());
		}
		Collections.sort(result.functions);
		Collections.sort(result.alias);
		return result;
	}

	public static DBFormat.NamedObject export(Brand object) {
		DBFormat.NamedObject result = new DBFormat.NamedObject();
		result.name = object.getName();
		result.description = object.getDescription();
		return result;
	}

	public static DBFormat.TypeOject export(Type object) {
		DBFormat.TypeOject result = new DBFormat.TypeOject();
		result.name = object.getName();
		result.description = object.getDescription();
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
		DBFormat.ProductObject result = new DBFormat.ProductObject();
		result.name = object.getName();
		result.brand = object.getBrandName();
		result.description = object.getDescription();
		result.image = object.getImage();
		result.price = object.getFormattedPrice();
		String sizeUnit = object.getSize_unit();
		if (sizeUnit != null && !sizeUnit.isEmpty()) {
			result.size = object.getSize() + " " + sizeUnit;
		}
		else {
			result.size = "";
		}
		Set<Type> types = object.getTypes();
		List<String> typesString = new ArrayList<>();
		for (Type type : types) {
			typesString.add(type.getName());
		}
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
