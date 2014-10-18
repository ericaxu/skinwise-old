package src.controllers.admin;

import src.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBFormat {
	public List<IngredientObject> ingredients = new ArrayList<>();
	public List<NamedObject> ingredient_functions = new ArrayList<>();
	public List<NamedObject> brands = new ArrayList<>();
	public List<NamedObject> types = new ArrayList<>();
	public List<ProductObject> products = new ArrayList<>();

	//Currently unused
	public List<IngredientAbbreviationObject> ingredient_abbreviations = new ArrayList<>();

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
		public Set<String> functions = new HashSet<>();
		public Set<String> names = new HashSet<>();

		@Override
		public void sanitize() {
			super.sanitize();
			cas_no = Util.notNull(cas_no).trim();
			ec_no = Util.notNull(ec_no).trim();
			restriction = Util.notNull(restriction).trim();
			if (functions == null) {
				functions = new HashSet<>();
			}
			if (names == null) {
				names = new HashSet<>();
			}
			functions.remove("");
			functions.remove(null);
			names.remove("");
			names.remove(null);
		}
	}

	public static class IngredientAbbreviationObject {
		public String shorthand;
		public String full;

		public void sanitize() {
			shorthand = Util.notNull(shorthand);
			full = Util.notNull(full);
		}
	}

	public static class ProductObject extends NamedObject {
		public String brand;
		public String type;
		public String key_ingredients;
		public String ingredients;

		@Override
		public void sanitize() {
			super.sanitize();
			brand = Util.notNull(brand).trim();
			type = Util.notNull(type).trim();
			ingredients = Util.notNull(ingredients).trim();
			key_ingredients = Util.notNull(key_ingredients).trim();
		}
	}
}
