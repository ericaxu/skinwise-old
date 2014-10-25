package src.models;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.commons.lang3.StringUtils;
import src.App;
import src.models.data.*;
import src.models.util.BaseModel;
import src.models.util.NamedModel;
import src.models.util.Relation;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemCache {
	public static class NamedIndex<T extends NamedModel> extends Idx<T> {
		private Map<String, T> names;
		private Searchable<T> search;

		public NamedIndex(ReadWriteLock lock, NamedGetter<T> ngetter, Getter<T> igetter) {
			super(lock, igetter);
			this.names = new HashMap<>();
			this.search = new Searchable<>(this.names, lock);
		}

		protected String key(T input) {
			return input.getName().toLowerCase();
		}

		@Override
		protected void cache(List<T> list) {
			lock.writeLock().lock();
			try {
				super.cache(list);
				names.clear();
				search.reset();
				for (T object : list) {
					names.put(key(object), object);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public List<T> search(String query, int limit, boolean fullSearch) {
			return search.search(query, limit, fullSearch);
		}

		public void updateNameAndSave(T object, String name) {
			lock.writeLock().lock();
			try {
				if (!Objects.equals(object.getName(), name)) {
					names.remove(key(object));
					object.setName(name);
					search.reset();
				}
				object.save();
				update(object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		@Override
		public void update(T object) {
			String key = key(object);
			lock.writeLock().lock();
			try {
				super.update(object);
				names.put(key, object);
				search.update(key);
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
			}
			finally {
				lock.readLock().unlock();
			}
		}
	}

	public static class ProductIndex extends NamedIndex<Product> {
		private Map<Brand, Map<String, Product>> products;

		public ProductIndex(ReadWriteLock lock, NamedGetter<Product> ngetter, Getter<Product> igetter) {
			super(lock, ngetter, igetter);
			this.products = new HashMap<>();
		}

		@Override
		protected String key(Product input) {
			return (input.getBrandName() + " " + input.getName()).toLowerCase();
		}

		@Override
		protected void cache(List<Product> list) {
			lock.writeLock().lock();
			try {
				super.cache(list);
				products.clear();
				Collection<Brand> brands = App.cache().brands.all();

				for (Brand brand : brands) {
					products.put(brand, new HashMap<>());
				}

				for (Product object : list) {
					getOrCreateMap(object.getBrand()).put(super.key(object), object);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void updateAndSave(Product object, Brand brand, String name) {
			lock.writeLock().lock();
			try {
				if (object.getBrand() != null && object.getName() != null) {
					if (!Objects.equals(object.getBrand(), brand) || !Objects.equals(object.getName(), name)) {
						getOrCreateMap(object.getBrand()).remove(super.key(object));
					}
				}
				object.setBrand(brand);
				super.updateNameAndSave(object, name);
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
				products.get(object.getBrand()).put(super.key(object), object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public Product get(Brand brand, String name) {
			lock.readLock().lock();
			try {
				String key = name.toLowerCase();
				Map<String, Product> map = getOrCreateMap(brand);
				if (map.containsKey(key)) {
					return map.get(key);
				}
				return null;
			}
			finally {
				lock.readLock().unlock();
			}
		}

		private Map<String, Product> getOrCreateMap(Brand brand) {
			if (brand == null) {
				return null;
			}
			Map<String, Product> map = products.get(brand);
			if (map == null) {
				map = new HashMap<>();
				products.put(brand, map);
			}
			return map;
		}
	}

	public static class Idx<T extends BaseModel> {
		protected ReadWriteLock lock;
		private TLongObjectMap<T> index;
		private List<T> all;
		private Getter<T> getter;

		public Idx(ReadWriteLock lock, Getter<T> getter) {
			this.lock = lock;
			this.index = new TLongObjectHashMap<>();
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
				all = list;
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
				all.add(item);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public Collection<T> all() {
			lock.readLock().lock();
			try {
				return all;
			}
			finally {
				lock.readLock().unlock();
			}
		}

		public T get(long id) {
			return get(id, false);
		}

		public T get(long id, boolean update) {
			lock.readLock().lock();
			try {
				T result;
				if (index.containsKey(id)) {
					result = index.get(id);
					if (update) {
						result.syncRefresh();
					}
					return result;
				}
				if (update) {
					result = getter.byId(id);
					if (result == null) {
						return null;
					}
					update(result);
					return result;
				}
			}
			finally {
				lock.readLock().unlock();
			}
			return null;
		}
	}

	public static class RTableIdx<L extends BaseModel, R extends BaseModel> {
		protected ReadWriteLock lock;
		private TLongObjectMap<Set<R>> left_index;
		private TLongObjectMap<Set<L>> right_index;
		private RGetter<L, R> getter;
		private Getter<L> left_getter;
		private Getter<R> right_getter;

		public RTableIdx(ReadWriteLock lock, RGetter<L, R> getter, Getter<L> l_getter, Getter<R> r_getter) {
			this.lock = lock;
			this.left_index = new TLongObjectHashMap<>();
			this.right_index = new TLongObjectHashMap<>();
			this.getter = getter;
			this.left_getter = l_getter;
			this.right_getter = r_getter;
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

		protected void cache(List<? extends Relation> list) {
			lock.writeLock().lock();
			try {
				left_index.clear();
				right_index.clear();
				for (Relation object : list) {
					L left = left_getter.byId(object.getLeftId());
					R right = right_getter.byId(object.getRightId());
					getL(left.getId(), false).add(right);
					getR(right.getId(), false).add(left);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void remove(Relation object) {
			L left = left_getter.byId(object.getLeftId());
			R right = right_getter.byId(object.getRightId());
			getL(left.getId(), true).remove(right);
			getR(right.getId(), true).remove(left);
		}

		public void add(Relation object) {
			L left = left_getter.byId(object.getLeftId());
			R right = right_getter.byId(object.getRightId());
			getL(left.getId(), true).add(right);
			getR(right.getId(), true).add(left);
		}

		public Set<L> getR(long right_id) {
			return getR(right_id, true);
		}

		public Set<R> getL(long left_id) {
			return getL(left_id, true);
		}

		private Set<L> getR(long right_id, boolean fetch) {
			if (!right_index.containsKey(right_id)) {
				if (fetch) {
					right_index.put(right_id, getter.byRight(right_id));
				}
				else {
					right_index.put(right_id, new HashSet<L>());
				}
			}
			return right_index.get(right_id);
		}

		private Set<R> getL(long left_id, boolean fetch) {
			if (!left_index.containsKey(left_id)) {
				if (fetch) {
					left_index.put(left_id, getter.byLeft(left_id));
				}
				else {
					left_index.put(left_id, new HashSet<R>());
				}
			}
			return left_index.get(left_id);
		}
	}

	public static interface Getter<T extends BaseModel> {
		public List<T> all();

		public T byId(Long id);
	}

	public static interface NamedGetter<T extends NamedModel> {
		public T byName(String name);
	}

	private static abstract class RGetter<L extends BaseModel, R extends BaseModel> {
		public abstract Set<R> byLeft(long left_id);

		public abstract Set<L> byRight(long right_id);

		public abstract List<? extends Relation> all();
	}

	private static class ProductIngredientGetter extends RGetter<Product, IngredientName> {
		public Set<IngredientName> byLeft(long left_id) {
			List<ProductIngredient> result = ProductIngredient.byProductId(left_id);
			Set<IngredientName> set = new HashSet<>();
			for (ProductIngredient relation : result) {
				set.add(relation.getIngredient_name());
			}
			return set;
		}

		public Set<Product> byRight(long right_id) {
			List<ProductIngredient> result = ProductIngredient.byIngredientNameId(right_id);
			Set<Product> set = new HashSet<>();
			for (ProductIngredient relation : result) {
				set.add(relation.getProduct());
			}
			return set;
		}

		public List<? extends Relation> all() {
			return ProductIngredient.all();
		}
	}

	public static class Matcher {
		private MemCache cache;
		public Map<IngredientName, Set<String>> ingredient_name_word_index = new HashMap<>();

		public Matcher(MemCache cache) {
			this.cache = cache;
		}

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
			cache.ingredient_names.search.reset();
			cache.lock.readLock().lock();
			ingredient_name_word_index.clear();
			cache.lock.readLock().unlock();
		}

		public static List<String> splitIngredients(String ingredient_string) {
			ingredient_string = ingredient_string
					.replaceAll("[0-9\\.]+\\s*%", "")
					.replaceAll("\\(\\s*\\)", "");

			String[] ingredients = ingredient_string.split(",(?=[^\\)]*(?:\\(|$))");

			List<String> result = new ArrayList<>();
			for (String ingredient : ingredients) {
				ingredient = ingredient.trim().toLowerCase();
				ingredient = StringUtils.strip(ingredient, "\t ,./?`~!@#$%^&*;:");
				if (!ingredient.isEmpty()) {
					result.add(ingredient);
				}
			}

			return result;
		}

		public static List<String> splitIngredient(String ingredient) {
			String[] words = ingredient.toLowerCase().split("[^a-zA-Z0-9]");
			List<String> result = new ArrayList<>();
			for (String word : words) {
				word = word.trim().toLowerCase();
				if (!word.isEmpty()) {
					result.add(word);
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

			/*
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
			*/

			List<IngredientName> result = cache.ingredient_names.search(input, 1, true);
			if (!result.isEmpty()) {
				name = result.get(0);
			}

			return name;
		}
	}

	public static class Searchable<T extends NamedModel> {
		private Map<String, T> names;
		private ReadWriteLock lock;
		private SearchEngine<T> search;

		public Searchable(Map<String, T> names, ReadWriteLock lock) {
			this.names = names;
			this.lock = lock;
		}

		public List<T> search(String query, int limit, boolean fullSearch) {
			query = query.toLowerCase();
			List<T> result;
			lock.writeLock().lock();
			try {
				if (search == null) {
					search = new SearchEngine<>();
					search.init(names);
				}
				if (fullSearch) {
					result = search.fullSearch(query, limit);
				}
				else {
					result = search.partialSearch(query, limit);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
			if (result == null || result.isEmpty()) {
				return new ArrayList<>();
			}
			List<T> filteredResult = new ArrayList<>();
			for (T object : result) {
				if (object != null) {
					filteredResult.add(object);
				}
			}
			return filteredResult;
		}

		public void update(String key) {
			lock.writeLock().lock();
			try {
				if (search != null) {
					search.update(key);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void reset() {
			lock.writeLock().lock();
			try {
				search = null;
			}
			finally {
				lock.writeLock().unlock();
			}
		}
	}

	private TLongObjectMap<Set<IngredientName>> name_map;

	public Set<IngredientName> getNamesForIngredient(long ingredient_id) {
		if (!name_map.containsKey(ingredient_id)) {
			name_map.put(ingredient_id, IngredientName.byIngredientId(ingredient_id));
		}
		return name_map.get(ingredient_id);
	}

	private ReadWriteLock lock;
	public NamedIndex<Function> functions;
	public NamedIndex<Brand> brands;
	public NamedIndex<ProductType> types;
	public NamedIndex<Ingredient> ingredients;
	public NamedIndex<IngredientName> ingredient_names;
	public RTableIdx<Product, IngredientName> product_ingredient;
	public ProductIndex products;
	public Matcher matcher;

	public MemCache() {
		lock = new ReentrantReadWriteLock();
		functions = new NamedIndex<>(lock, Function.find, Function.find);
		brands = new NamedIndex<>(lock, Brand.find, Brand.find);
		types = new NamedIndex<>(lock, ProductType.find, ProductType.find);
		ingredients = new NamedIndex<>(lock, Ingredient.find, Ingredient.find);
		ingredient_names = new NamedIndex<>(lock, IngredientName.find, IngredientName.find);
		products = new ProductIndex(lock, Product.find, Product.find);
		product_ingredient = new RTableIdx<>(lock, new ProductIngredientGetter(),
				Product.find, IngredientName.find);
		matcher = new Matcher(this);
		name_map = new TLongObjectHashMap<>();
	}

	public void init() {
		System.gc();
		lock.writeLock().lock();
		functions.cache();
		brands.cache();
		types.cache();
		ingredients.cache();
		ingredient_names.cache();
		products.cache();
		product_ingredient.cache();
		name_map.clear();
		lock.writeLock().unlock();
	}
}
