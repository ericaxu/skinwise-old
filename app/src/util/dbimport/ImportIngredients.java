package src.util.dbimport;

import src.models.Ingredient;
import src.models.IngredientFunction;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.Set;

public class ImportIngredients {
	private static final String TAG = "ImportIngredients";

	public static void importDB(String json) throws IOException {
		IngredientFormat result = Json.deserialize(json, IngredientFormat.class);

		//Import functions
		for (IngredientFunctionObject object : result.ingredient_functions) {
			createOrUpdate(object);
		}

		//Import ingredients
		for (IngredientObject object : result.ingredients) {
			createOrUpdate(object);
		}
	}

	private static void createOrUpdate(IngredientFunctionObject object) {
		object.description = Util.notNull(object.description);
		object.name = Util.notNull(object.name).toLowerCase();
		IngredientFunction function = IngredientFunction.byName(object.name);
		if (function == null) {
			function = new IngredientFunction(object.name, object.description);
		}

		String oldDescription = function.getDescription();
		if (oldDescription == null || oldDescription.isEmpty()) {
			function.setDescription(object.description);
		}
		else if (!oldDescription.equals(object.description)) {
			Logger.error(TAG, "Function description not matching! " +
					oldDescription + "|" + object.description);
		}

		function.save();
	}

	private static void createOrUpdate(IngredientObject object) {
		object.inci_name = Util.notNull(object.inci_name).toLowerCase();
		object.description = Util.notNull(object.description);
		object.cas_no = Util.notNull(object.cas_no);
		object.restriction = Util.notNull(object.restriction);

		Ingredient ingredient = Ingredient.byINCIName(object.inci_name);
		if (ingredient == null) {
			ingredient = new Ingredient(object.inci_name,
					object.cas_no,
					object.description
			);
		}

		String oldDescription = ingredient.getDescription();
		if (oldDescription == null || oldDescription.isEmpty()) {
			ingredient.setDescription(object.description);
		}
		else if (!oldDescription.equals(object.description)) {
			Logger.error(TAG, "Ingredient description not matching! " +
					oldDescription + "|" + object.description);
		}

		Set<IngredientFunction> functionList = ingredient.getFunctions();
		String[] functions = object.functions.split(",");
		for (String function : functions) {
			function = function.trim();
			if (function.isEmpty()) {
				continue;
			}
			IngredientFunction function1 = IngredientFunction.byName(function.toLowerCase());
			if (function1 == null) {
				Logger.debug(TAG, object.functions);
			}
			else if (!functionList.contains(function1)) {
				functionList.add(function1);
			}
		}
		ingredient.addFunctions(functionList);

		ingredient.save();
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
