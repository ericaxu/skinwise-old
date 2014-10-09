package src.util.dbimport;

import src.models.Ingredient;
import src.models.IngredientFunction;
import src.models.IngredientName;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class INCI {
	private static final String TAG = "INCI";

	public static void importDB(String json) throws IOException {
		INCIFormat result = Json.deserialize(json, INCIFormat.class);

		//Import functions
		for (INCIIngredientFunction object : result.ingredient_functions) {
			createOrUpdate(object);
		}

		//Import ingredients
		for (INCIIngredient object : result.ingredients) {
			createOrUpdate(object);
		}
	}

	private static void createOrUpdate(INCIIngredientFunction function) {
		function.description = Util.notNull(function.description);
		function.name = Util.notNull(function.name).toLowerCase();
		IngredientFunction object = IngredientFunction.byName(function.name);
		if (object == null) {
			object = new IngredientFunction(function.name, function.description);
		}

		String oldDescription = object.getDescription();
		if (oldDescription == null || oldDescription.isEmpty()) {
			object.setDescription(function.description);
		}
		else if (!oldDescription.equals(function.description)) {
			Logger.error(TAG, "Function description not matching! " +
					oldDescription + "|" + function.description);
		}

		object.save();
	}

	private static void createOrUpdate(INCIIngredient ingredient) {
		ingredient.inci_name = Util.notNull(ingredient.inci_name).toLowerCase();
		ingredient.description = Util.notNull(ingredient.description);
		ingredient.cas_no = Util.notNull(ingredient.cas_no);
		ingredient.restriction = Util.notNull(ingredient.restriction);

		Ingredient object = Ingredient.byINCIName(ingredient.inci_name);
		if (object == null) {
			object = new Ingredient(ingredient.inci_name,
					ingredient.cas_no,
					ingredient.description
			);
		}

		String oldDescription = object.getDescription();
		if (oldDescription == null || oldDescription.isEmpty()) {
			object.setDescription(ingredient.description);
		}
		else if (!oldDescription.equals(ingredient.description)) {
			Logger.error(TAG, "Ingredient description not matching! " +
					oldDescription + "|" + ingredient.description);
		}
		
		Set<IngredientFunction> functionList = object.getFunctions();
		String[] functions = ingredient.functions.split(",");
		for (String function : functions) {
			function = function.trim();
			if (function.isEmpty()) {
				continue;
			}
			IngredientFunction function1 = IngredientFunction.byName(function.toLowerCase());
			if (function1 == null) {
				Logger.debug(TAG, ingredient.functions);
			}
			else if (!functionList.contains(function1)) {
				functionList.add(function1);
			}
		}
		object.addFunctions(functionList);

		object.save();
	}

	public static class INCIFormat {
		public INCIIngredient[] ingredients;
		public INCIIngredientFunction[] ingredient_functions;
		public INCIIngredientAbbreviation[] ingredient_abbreviations;
	}

	public static class INCIIngredient {
		public String inci_name;
		public String inn_name;
		public String ph_eur_name;
		public String cas_no;
		public String ec_no;
		public String iupac_name;
		public String description;
		public String restriction;
		public String functions;
	}

	public static class INCIIngredientFunction {
		public String name;
		public String description;
	}

	public static class INCIIngredientAbbreviation {
		public String shorthand;
		public String full;
	}
}
