package src.util.dbimport;

import src.models.Ingredient;
import src.models.IngredientFunction;
import src.models.IngredientName;
import src.util.Json;
import src.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class INCI {
	public static void importDB(String json) throws IOException {
		INCIFormat result = Json.deserialize(json, INCIFormat.class);
		for (INCIIngredientFunction object : result.ingredient_functions) {
			IngredientFunction ingredientFunction = IngredientFunction.byName(object.name.toLowerCase());
			if (ingredientFunction == null) {
				ingredientFunction = new IngredientFunction(object.name.toLowerCase(), object.description);
				ingredientFunction.save();
			}
		}

		for (INCIIngredient object : result.ingredients) {
			Ingredient ingredient = Ingredient.byINCIName(object.inci_name.toLowerCase());
			if (ingredient == null) {
				Set<IngredientFunction> functionList = new HashSet<>();
				String[] functions = object.functions.split("(,|/|;)");
				for (String function : functions) {
					function = function.trim();
					if(function.isEmpty()) {
						continue;
					}
					IngredientFunction function1 = IngredientFunction.byName(function.toLowerCase());
					if (function1 == null) {
						Logger.debug("TAG", object.functions);
					}
					else if (!functionList.contains(function1)) {
						functionList.add(function1);
					}
				}
				ingredient = new Ingredient(object.inci_name.toLowerCase(),
						object.cas_no,
						object.restriction,
						new ArrayList<IngredientName>(),
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
