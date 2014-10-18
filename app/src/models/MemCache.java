package src.models;

import src.App;
import src.models.data.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemCache {
	public static class NamedIndex<T extends NamedModel> extends Idx<T> {
		private Map<String, T> names;
		private NamedGetter<T> getter;

		public NamedIndex(ReadWriteLock lock, NamedGetter<T> getter) {
			super(lock, getter);
			this.names = new HashMap<>();
			this.getter = getter;
		}

		private String key(T input) {
			lock.readLock().lock();
			try {
				return input.getName().toLowerCase();
			}
			finally {
				lock.readLock().unlock();
			}
		}

		@Override
		protected void cache(List<T> list) {
			lock.writeLock().lock();
			try {
				super.cache(list);
				names.clear();
				for (T object : list) {
					names.put(key(object), object);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void updateNameAndSave(T object, String newname) {
			lock.writeLock().lock();
			try {
				names.remove(key(object));
				object.setName(newname);
				object.save();
				update(object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		@Override
		public void update(T object) {
			lock.writeLock().lock();
			try {
				super.update(object);
				names.put(key(object), object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public T get(String name) {
			lock.readLock().lock();
			try {
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
			finally {
				lock.readLock().unlock();
			}
		}
	}

	public static class ProductIndex extends Idx<Product> {
		private Map<Brand, Map<String, Product>> products;

		public ProductIndex(ReadWriteLock lock, Getter<Product> getter) {
			super(lock, getter);
			this.products = new HashMap<>();
		}

		private String key(Product input) {
			lock.readLock().lock();
			try {
				return input.getName().toLowerCase();
			}
			finally {
				lock.readLock().unlock();
			}
		}

		@Override
		protected void cache(List<Product> list) {
			lock.writeLock().lock();
			try {
				super.cache(list);
				products.clear();
				List<Brand> brands = Brand.all();

				for (Brand brand : brands) {
					products.put(brand, new HashMap<>());
				}

				for (Product object : list) {
					products.get(object.getBrand()).put(key(object), object);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void updateAndSave(Product object, Brand brand, String name) {
			lock.writeLock().lock();
			try {
				products.get(object.getBrand()).remove(key(object));
				object.setBrand(brand);
				object.setName(name);
				object.save();
				update(object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		@Override
		public void update(Product object) {
			lock.writeLock().lock();
			try {
				super.update(object);
				products.get(object.getBrand()).put(key(object), object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public Product get(Brand brand, String name) {
			lock.readLock().lock();
			try {
				String key = name.toLowerCase();
				Map<String, Product> map = products.get(brand);
				if (map.containsKey(key)) {
					return map.get(key);
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
			finally {
				lock.readLock().unlock();
			}
		}
	}

	public static class Idx<T extends BaseModel> {
		protected ReadWriteLock lock;
		private Map<Long, T> index;
		private Getter<T> getter;

		public Idx(ReadWriteLock lock, Getter<T> getter) {
			this.lock = lock;
			this.index = new HashMap<>();
			this.getter = getter;
		}

		public void cache() {
			lock.writeLock().lock();
			try {
				cache(getter.all());
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		protected void cache(List<T> list) {
			lock.writeLock().lock();
			try {
				index.clear();
				for (T object : list) {
					index.put(object.getId(), object);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void update(T item) {
			lock.writeLock().lock();
			try {
				index.put(item.getId(), item);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public Collection<T> all() {
			lock.readLock().lock();
			try {
				return index.values();
			}
			finally {
				lock.readLock().unlock();
			}
		}

		public T get(long id) {
			lock.readLock().lock();
			try {
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
			finally {
				lock.readLock().unlock();
			}
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

	private ReadWriteLock lock;
	public NamedIndex<Function> functions;
	public NamedIndex<Brand> brands;
	public NamedIndex<ProductType> types;
	public NamedIndex<Ingredient> ingredients;
	public NamedIndex<IngredientName> ingredient_names;
	public ProductIndex products;
	public Matcher matcher;

	public MemCache() {
		lock = new ReentrantReadWriteLock();
		functions = new NamedIndex<>(lock, new FunctionGetter());
		brands = new NamedIndex<>(lock, new BrandGetter());
		types = new NamedIndex<>(lock, new ProductTypeGetter());
		ingredients = new NamedIndex<>(lock, new IngredientGetter());
		ingredient_names = new NamedIndex<>(lock, new IngredientNameGetter());
		products = new ProductIndex(lock, new ProductGetter());
		matcher = new Matcher();
	}

	public void init() {
		System.gc();
		functions.cache();
		brands.cache();
		types.cache();
		ingredients.cache();
		ingredient_names.cache();
		products.cache();
	}
}
