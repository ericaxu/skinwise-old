package src.controllers.admin;

import src.util.Util;

import java.util.*;

public class DBFormat {
	public Map<String, IngredientObject> ingredients = new HashMap<>();
	public Map<String, NamedObject> ingredient_functions = new HashMap<>();
	public Map<String, NamedObject> brands = new HashMap<>();
	public Map<String, NamedObject> types = new HashMap<>();
	public Map<String, ProductObject> products = new HashMap<>();

	public static class NamedObject {
		public String name;
		public String description;

		public void sanitize() {
			name = Util.notNull(name).trim();
			description = Util.notNull(description).trim();
		}
	}

	public static class IngredientObject extends NamedObject {
		public String cas_no;
		public String ec_no;
		public String restriction;
		public List<String> functions = new ArrayList<>();
		public List<String> alias = new ArrayList<>();

		@Override
		public void sanitize() {
			super.sanitize();
			cas_no = Util.notNull(cas_no).trim();
			ec_no = Util.notNull(ec_no).trim();
			restriction = Util.notNull(restriction).trim();
			if (functions == null) {
				functions = new ArrayList<>();
			}
			if (alias == null) {
				alias = new ArrayList<>();
			}
			functions.remove("");
			functions.remove(null);
			alias.remove("");
			alias.remove(null);
			functions = new ArrayList<>(new LinkedHashSet<>(functions));
			alias = new ArrayList<>(new LinkedHashSet<>(alias));
		}
	}

	/*
	public static class IngredientAbbreviationObject {
		public String shorthand;
		public String full;

		public void sanitize() {
			shorthand = Util.notNull(shorthand);
			full = Util.notNull(full);
		}
	}
	*/

	public static class ProductObject extends NamedObject {
		public String brand;
		public String type;
		public String key_ingredients;
		public String ingredients;
		public String image;

		@Override
		public void sanitize() {
			super.sanitize();
			brand = Util.notNull(brand).trim();
			type = Util.notNull(type).trim();
			ingredients = Util.notNull(ingredients).trim();
			key_ingredients = Util.notNull(key_ingredients).trim();
			image = Util.notNull(image).trim();
		}
	}
}
