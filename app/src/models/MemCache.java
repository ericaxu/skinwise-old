package src.models;

import src.App;
import src.models.data.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemCache {
	public static class NamedIndex<T extends NamedModel> extends Idx<T> {
		private Map<String, T> names;
		private NamedGetter<T> getter;

		public NamedIndex(NamedGetter<T> getter) {
			super(getter);
			this.names = new ConcurrentHashMap<>();
			this.getter = getter;
		}

		private String key(T input) {
			return input.getName().toLowerCase();
		}

		@Override
		protected void cache(List<T> list) {
			super.cache(list);
			names.clear();
			for (T object : list) {
				names.put(key(object), object);
			}
		}

		public void updateNameAndSave(T object, String newname) {
			names.remove(key(object));
			object.setName(newname);
			object.save();
			update(object);
		}

		@Override
		public void update(T object) {
			super.update(object);
			names.put(key(object), object);
		}

		public T get(String name) {
			String key = name.toLowerCase();
			if (names.containsKey(key)) {
				return names.get(key);
			}
			return null;
			/*
			T result = getter.byName(key);
			if (result == null) {
				return null;
			}
			update(result);
			return result;*/
		}
	}

	public static class Idx<T extends BaseModel> {
		private Map<Long, T> index;
		private Getter<T> getter;

		public Idx(Getter<T> getter) {
			this.index = new ConcurrentHashMap<>();
			this.getter = getter;
		}

		public void cache() {
			cache(getter.all());
		}

		protected void cache(List<T> list) {
			index.clear();
			for (T object : list) {
				index.put(object.getId(), object);
			}
		}

		public void update(T item) {
			index.put(item.getId(), item);
		}

		public Collection<T> all() {
			return index.values();
		}

		public T get(long id) {
			if (index.containsKey(id)) {
				return index.get(id);
			}
			return null;
			/*
			T result = getter.byId(id);
			if (result == null) {
				return null;
			}
			update(result);
			return result;
			*/
		}
	}

	private static abstract class Getter<T extends BaseModel> {
		public abstract List<T> all();

		public abstract T byId(long id);
	}

	private static abstract class NamedGetter<T extends NamedModel> extends Getter<T> {
		public abstract T byName(String name);
	}

	private static class FunctionGetter extends NamedGetter<Function> {
		@Override
		public Function byName(String name) {
			return Function.byName(name);
		}

		@Override
		public List<Function> all() {
			return Function.all();
		}

		@Override
		public Function byId(long id) {
			return Function.byId(id);
		}
	}

	private static class BrandGetter extends NamedGetter<Brand> {
		@Override
		public Brand byName(String name) {
			return Brand.byName(name);
		}

		@Override
		public List<Brand> all() {
			return Brand.all();
		}

		@Override
		public Brand byId(long id) {
			return Brand.byId(id);
		}
	}

	private static class ProductTypeGetter extends NamedGetter<ProductType> {
		@Override
		public ProductType byName(String name) {
			return ProductType.byName(name);
		}

		@Override
		public List<ProductType> all() {
			return ProductType.all();
		}

		@Override
		public ProductType byId(long id) {
			return ProductType.byId(id);
		}
	}

	private static class IngredientGetter extends NamedGetter<Ingredient> {
		@Override
		public Ingredient byName(String name) {
			return Ingredient.byName(name);
		}

		@Override
		public List<Ingredient> all() {
			return Ingredient.all();
		}

		@Override
		public Ingredient byId(long id) {
			return Ingredient.byId(id);
		}
	}

	private static class IngredientNameGetter extends NamedGetter<IngredientName> {
		@Override
		public IngredientName byName(String name) {
			return IngredientName.byName(name);
		}

		@Override
		public List<IngredientName> all() {
			return IngredientName.all();
		}

		@Override
		public IngredientName byId(long id) {
			return IngredientName.byId(id);
		}
	}

	private static class ProductGetter extends Getter<Product> {
		@Override
		public List<Product> all() {
			return Product.all();
		}

		@Override
		public Product byId(long id) {
			return Product.byId(id);
		}
	}

	public static class Matcher {
		public Map<IngredientName, Set<String>> ingredient_name_word_index = new HashMap<>();

		private void cacheIngredientName(IngredientName name) {
			String key = name.getName().toLowerCase();
			String[] words = key.split("[^a-zA-Z0-9]");
			Set<String> set = new HashSet<>(Arrays.asList(words));
			set.remove("");
			ingredient_name_word_index.put(name, set);
		}

		public void cache(Collection<IngredientName> names) {
			clear();
			for (IngredientName name : names) {
				cacheIngredientName(name);
			}
		}

		public void clear() {
			ingredient_name_word_index.clear();
		}

		public List<String> splitIngredients(String ingredient_string) {
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

		public List<IngredientName> matchAllIngredientNames(String input) {
			List<IngredientName> matches = new ArrayList<>();
			Set<IngredientName> matchSet = new HashSet<>();
			List<String> ingredients = splitIngredients(input);
			for (String ingredient : ingredients) {
				IngredientName name = matchIngredientName(ingredient);
				if (!matchSet.contains(name)) {
					matches.add(name);
					matchSet.add(name);
				}
			}
			return matches;
		}

		public IngredientName matchIngredientName(String input) {
			IngredientName name = App.cache().ingredient_names.get(input);
			if (name != null) {
				return name;
			}

			String[] words = input.toLowerCase().split("[^a-zA-Z0-9]");

			for (Map.Entry<IngredientName, Set<String>> entry : ingredient_name_word_index.entrySet()) {
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
					name = entry.getKey();
					break;
				}
			}

			if (name == null) {
				name = new IngredientName();
			}
			else if (!name.getName().equalsIgnoreCase(input)) {
				Ingredient ingredient = name.getIngredient();
				name = new IngredientName();
				name.setIngredient(ingredient);
			}
			else {
				return name;
			}
			name.setName(input);
			name.save();
			App.cache().ingredient_names.update(name);
			cacheIngredientName(name);
			return name;
		}
	}

	public NamedIndex<Function> functions;
	public NamedIndex<Brand> brands;
	public NamedIndex<ProductType> types;
	public NamedIndex<Ingredient> ingredients;
	public NamedIndex<IngredientName> ingredient_names;
	public Idx<Product> products;
	public Matcher matcher;

	public MemCache() {
		functions = new NamedIndex<>(new FunctionGetter());
		brands = new NamedIndex<>(new BrandGetter());
		types = new NamedIndex<>(new ProductTypeGetter());
		ingredients = new NamedIndex<>(new IngredientGetter());
		ingredient_names = new NamedIndex<>(new IngredientNameGetter());
		products = new Idx<>(new ProductGetter());
		matcher = new Matcher();
	}

	public void init() {
		functions.cache();
		brands.cache();
		types.cache();
		ingredients.cache();
		ingredient_names.cache();
		products.cache();
	}
}
