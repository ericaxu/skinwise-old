package util.dbimport;

import models.Ingredient;
import models.IngredientFunction;
import util.Json;
import util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class INCI {
	public static void importDB(String json) throws IOException {
		INCIFormat result = Json.deserialize(json, INCIFormat.class);
		for (INCIIngredientFunction object : result.ingredient_functions) {
			IngredientFunction ingredientFunction = IngredientFunction.byName(object.name.toUpperCase());
			if (ingredientFunction == null) {
				ingredientFunction = new IngredientFunction(object.name.toUpperCase(), object.description);
				ingredientFunction.save();
			}
		}

		for (INCIIngredient object : result.ingredients) {
			Ingredient ingredient = Ingredient.byINCIName(object.inci_name);
			if (ingredient == null) {
				Set<IngredientFunction> functionList = new HashSet<>();
				String[] functions = object.functions.split("(,|/|;)");
				for (String function : functions) {
					function = function.trim();
					if(function.isEmpty()) {
						continue;
					}
					IngredientFunction function1 = IngredientFunction.byName(function.toUpperCase());
					if (function1 == null) {
						Logger.debug("TAG", object.functions);
					}
					else if (!functionList.contains(function1)) {
						functionList.add(function1);
					}
				}
				ingredient = new Ingredient(object.inci_name,
						object.cas_no,
						object.restriction,
						new ArrayList<>(),
						new ArrayList<>(functionList)
				);
				ingredient.save();
			}
		}
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
