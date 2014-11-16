package src.controllers.admin;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import src.util.Util;

import java.util.*;

@JsonPropertyOrder(alphabetic = true)
public class DBFormat {
	public Map<String, IngredientObject> ingredients = new HashMap<>();
	public Map<String, NamedObject> functions = new HashMap<>();
	public Map<String, NamedObject> benefits = new HashMap<>();
	public Map<String, NamedObject> brands = new HashMap<>();
	public Map<String, TypeOject> types = new HashMap<>();
	public Map<String, ProductObject> products = new HashMap<>();

	@JsonPropertyOrder(alphabetic = true)
	public static class DBObject {
		public long id;

		public void sanitize() {
			if (id < 0) {
				id = 0;
			}
		}

		public List<String> sanitize(List<String> input) {
			if (input == null) {
				input = new ArrayList<>();
			}
			input.remove("");
			input.remove(null);
			return new ArrayList<>(new LinkedHashSet<>(input));
		}
	}

	@JsonPropertyOrder(alphabetic = true)
	public static class NamedObject extends DBObject {
		public String name;
		public String description;

		public void sanitize() {
			name = Util.notNull(name).trim();
			description = Util.notNull(description).trim();
		}
	}

	@JsonPropertyOrder(alphabetic = true)
	public static class IngredientObject extends NamedObject {
		public String display_name;
		public String cas_no;
		public String ec_no;
		public String restriction;
		public long popularity;
		public boolean active;
		public List<String> functions = new ArrayList<>();
		public List<String> benefits = new ArrayList<>();
		public List<String> alias = new ArrayList<>();

		@Override
		public void sanitize() {
			super.sanitize();
			display_name = Util.notNull(display_name).trim();
			cas_no = Util.notNull(cas_no).trim();
			ec_no = Util.notNull(ec_no).trim();
			restriction = Util.notNull(restriction).trim();
			functions = sanitize(functions);
			benefits = sanitize(benefits);
			alias = sanitize(alias);
		}
	}

	@JsonPropertyOrder(alphabetic = true)
	public static class ProductObject extends NamedObject {
		public String brand;
		public String types;
		public String key_ingredients;
		public String ingredients;
		public String image;
		public long popularity;
		public String price;
		public String size;

		@Override
		public void sanitize() {
			super.sanitize();
			brand = Util.notNull(brand).trim();
			types = Util.notNull(types).trim();
			ingredients = Util.notNull(ingredients).trim();
			key_ingredients = Util.notNull(key_ingredients).trim();
			image = Util.notNull(image).trim();
			price = Util.notNull(price).trim();
			size = Util.notNull(size).trim();
		}
	}

	@JsonPropertyOrder(alphabetic = true)
	public static class TypeOject extends NamedObject {
		public String parent;

		public void sanitize() {
			super.sanitize();
			parent = Util.notNull(parent).trim();
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
