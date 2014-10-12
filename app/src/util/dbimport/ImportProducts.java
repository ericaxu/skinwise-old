package src.util.dbimport;

import src.models.data.History;
import src.models.data.ingredient.Ingredient;
import src.models.data.product.AllProduct;
import src.models.data.product.Product;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.*;

public class ImportProducts {
	private static final String TAG = "ImportProducts";

	public static synchronized Import.ImportResult importDB(String json) throws IOException {
		ProductFormat input = Json.deserialize(json, ProductFormat.class);

		ProductFormat valid = new ProductFormat();
		ProductFormat invalid = new ProductFormat();

		IngredientMatcher matcher = new IngredientMatcher();

		//Import products
		for (ProductObject object : input.products) {
			boolean isValid = createOrUpdate(object, matcher);
			if (isValid) {
				valid.products.add(object);
			}
			else {
				invalid.products.add(object);
			}
		}

		List<String> failedMatches = new ArrayList<>(matcher.getFailedMatches());
		Collections.sort(failedMatches, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.compare(o2.length(), o1.length());
			}
		});

		return new Import.ImportResult(valid, invalid, failedMatches);
	}

	private static boolean createOrUpdate(ProductObject object, IngredientMatcher matcher) {
		object.sanitize();

		List<Ingredient> ingredients = matcher.matchAll(object.ingredients);
		for (Ingredient ingredient : ingredients) {
			if (ingredient == null) {
				return false;
			}
		}

		Product target = Product.byBrandAndName(object.brand, object.name);
		long target_id = History.getTargetId(target);

		AllProduct result = new AllProduct(target_id, History.SUBMITTED_BY_SYSTEM);
		result.setName(object.name);
		result.setBrand(object.brand);
		result.setDescription(object.claims);
		result.setIngredients(object.ingredients);
		result.setKey_ingredients(object.key_ingredients);

		result.save();
		result.approve();

		return true;
	}

	private static class IngredientMatcher {
		Map<Ingredient, Set<String>> map = new HashMap<>();
		Set<String> failedMatches = new HashSet<>();

		public IngredientMatcher() {
			List<Ingredient> all = Ingredient.getAll();
			for (Ingredient ingredient : all) {
				String[] words = ingredient.getName().split("[^a-zA-Z0-9]");
				Set<String> set = new HashSet<>(Arrays.asList(words));
				set.remove("");
				map.put(ingredient, set);
			}
		}

		private List<String> splitIngredients(String ingredient_string) {
			ingredient_string = ingredient_string
					.replaceAll("[0-9\\.]+\\s*%", "")
					.replaceAll("\\(\\s*\\)", "");

			String[] ingredients = ingredient_string.split(",(?=[^\\)]*(?:\\(|$))");

			List<String> result = new ArrayList<>();
			for (String ingredient : ingredients) {
				ingredient = ingredient.trim().toLowerCase();
				if (!ingredient.isEmpty()) {
					result.add(ingredient);
				}
			}

			return result;
		}

		public List<Ingredient> matchAll(String input) {
			List<Ingredient> matches = new ArrayList<>();
			List<String> ingredients = splitIngredients(input);
			for (String ingredient : ingredients) {
				matches.add(match(ingredient));
			}
			return matches;
		}

		public Ingredient match(String input) {
			String[] words = input.split("[^a-zA-Z0-9]");
			for (Map.Entry<Ingredient, Set<String>> entry : map.entrySet()) {
				boolean allmatch = true;
				for (String word : words) {
					if (Objects.equals(word, "")) {
						continue;
					}
					if (!entry.getValue().contains(word)) {
						allmatch = false;
						break;
					}
				}
				if (allmatch) {
					return entry.getKey();
				}
			}
			failedMatches.add(input);
			return null;
		}

		public Set<String> getFailedMatches() {
			return failedMatches;
		}
	}

	public static class ProductFormat {
		public List<ProductObject> products = new ArrayList<>();
	}

	public static class ProductObject {
		public String name;
		public String brand;
		public String claims;
		public String key_ingredients;
		public String ingredients;
		public String matcher;

		public void sanitize() {
			name = Util.notNull(name);
			brand = Util.notNull(brand);
			claims = Util.notNull(claims);
			ingredients = Util.notNull(ingredients);
			key_ingredients = Util.notNull(key_ingredients);
			matcher = null;
		}
	}
}
