package src.controllers.admin;

import src.util.Util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
		public List<String> functions = new ArrayList<>();
		public List<String> names = new ArrayList<>();

		@Override
		public void sanitize() {
			super.sanitize();
			cas_no = Util.notNull(cas_no).trim();
			ec_no = Util.notNull(ec_no).trim();
			restriction = Util.notNull(restriction).trim();
			if (functions == null) {
				functions = new ArrayList<>();
			}
			if (names == null) {
				names = new ArrayList<>();
			}
			functions.remove("");
			functions.remove(null);
			names.remove("");
			names.remove(null);
			functions = new ArrayList<>(new LinkedHashSet<>(functions));
			names = new ArrayList<>(new LinkedHashSet<>(names));
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
