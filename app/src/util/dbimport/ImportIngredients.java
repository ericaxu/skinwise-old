package src.util.dbimport;

import src.models.data.Function;
import src.models.data.Ingredient;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.Set;

public class ImportIngredients {
	private static final String TAG = "ImportIngredients";

	public static synchronized void importDB(String path) throws IOException {
		String json = Util.readAll(path);
		IngredientFormat result = Json.deserialize(json, IngredientFormat.class);

		//Import functions
		for (IngredientFunctionObject object : result.ingredient_functions) {
			create(object);
		}

		//Import ingredients
		for (IngredientObject object : result.ingredients) {
			create(object);
		}
	}

	private static void create(IngredientFunctionObject object) {
		object.name = Util.notNull(object.name).toLowerCase();
		object.description = Util.notNull(object.description);

		Function result = Function.byName(object.name);

		if (result == null) {
			result = new Function();
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();
	}

	private static void create(IngredientObject object) {
		object.inci_name = Util.notNull(object.inci_name).toLowerCase();
		object.description = Util.notNull(object.description);
		object.cas_no = Util.notNull(object.cas_no);
		object.restriction = Util.notNull(object.restriction);
		object.functions = Util.notNull(object.functions);

		Ingredient result = Ingredient.byINCIName(object.inci_name);

		if (result == null) {
			result = new Ingredient();
		}

		result.setName(object.inci_name);
		result.setCas_number(object.cas_no);
		result.setDescription(object.description);

		Set<Function> functionList = result.getFunctions();
		String[] functions = object.functions.split(",");
		for (String function : functions) {
			function = function.trim();
			if (function.isEmpty()) {
				continue;
			}
			Function function1 = Function.byName(function.toLowerCase());
			if (function1 == null) {
				Logger.debug(TAG, object.functions);
			}
			functionList.add(function1);
		}
		result.setFunctions(functionList);

		result.save();
	}

	public static class IngredientFormat {
		public IngredientObject[] ingredients;
		public IngredientFunctionObject[] ingredient_functions;
		public IngredientAbbreviationObject[] ingredient_abbreviations;
	}

	public static class IngredientObject {
		public String inci_name;
		public String inn_name;
		public String ph_eur_name;
		public String iupac_name;
		public String cas_no;
		public String ec_no;
		public String description;
		public String restriction;
		public String functions;
	}

	public static class IngredientFunctionObject {
		public String name;
		public String description;
	}

	public static class IngredientAbbreviationObject {
		public String shorthand;
		public String full;
	}
}
