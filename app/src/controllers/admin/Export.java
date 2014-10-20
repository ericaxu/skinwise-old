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

		cache.init();

		Logger.debug(TAG, "Exporting functions");
		for (Function object : cache.functions.all()) {
			result.ingredient_functions.add(export(object));
		}

		Logger.debug(TAG, "Exporting brands");
		for (Brand object : cache.brands.all()) {
			result.brands.add(export(object));
		}

		Logger.debug(TAG, "Exporting ingredients");
		IngredientRelations relations = new IngredientRelations();
		relations.cacheIngredients(cache.ingredients.all(),
				cache.ingredient_names.all(),
				cache.functions.all());
		for (Ingredient object : cache.ingredients.all()) {
			result.ingredients.add(export(object, relations));
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

	public static DBFormat.IngredientObject export(Ingredient object, IngredientRelations relations) {
		DBFormat.IngredientObject result = new DBFormat.IngredientObject();
		result.name = object.getName();
		result.cas_no = object.getCas_number();
		result.description = object.getDescription();
		result.functions.addAll(relations.getIngredientFunctions(object));
		result.names.addAll(relations.getIngredientNames(object));
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

	public static class IngredientRelations {
		public Map<Ingredient, List<IngredientName>> ingredient_to_names = new HashMap<>();
		public Map<Ingredient, List<Function>> ingredient_to_function = new HashMap<>();

		private void cacheIngredientName(IngredientName name) {
			String key = name.getName().toLowerCase();
			String[] words = key.split("[^a-zA-Z0-9]");
			Set<String> set = new HashSet<>(Arrays.asList(words));
			set.remove("");
		}

		public void cacheIngredients(Collection<Ingredient> ingredients,
		                             Collection<IngredientName> names,
		                             Collection<Function> functions) {
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
	}
}
