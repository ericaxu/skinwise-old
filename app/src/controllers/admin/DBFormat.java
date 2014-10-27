package src.controllers.admin;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import src.util.Util;

import java.util.*;

@JsonPropertyOrder(alphabetic = true)
public class DBFormat {
	public Map<String, IngredientObject> ingredients = new HashMap<>();
	public Map<String, NamedObject> functions = new HashMap<>();
	public Map<String, NamedObject> brands = new HashMap<>();
	public Map<String, NamedObject> types = new HashMap<>();
	public Map<String, ProductObject> products = new HashMap<>();

	@JsonPropertyOrder(alphabetic = true)
	public static class NamedObject {
		public String name;
		public String description;

		public void sanitize() {
			name = Util.notNull(name).trim();
			description = Util.notNull(description).trim();
		}
	}

	@JsonPropertyOrder(alphabetic = true)
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

	@JsonPropertyOrder(alphabetic = true)
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
}
