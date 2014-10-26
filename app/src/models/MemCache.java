package src.models;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.commons.lang3.StringUtils;
import src.App;
import src.models.data.*;
import src.models.util.BaseModel;
import src.models.util.ManyToMany;
import src.models.util.NamedModel;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemCache {

	//Index

	public static class BaseIndex<T extends BaseModel> {
		protected ReadWriteLock lock;
		private Getter<T> getter;
		//Containers
		private TLongObjectMap<T> index;
		private List<T> all;

		public BaseIndex(Getter<T> getter) {
			this.lock = new ReentrantReadWriteLock();
			this.getter = getter;
			this.index = new TLongObjectHashMap<>();
			this.all = new ArrayList<>();
		}

		//Get

		public List<T> all() {
			lock.readLock().lock();
			try {
				return all;
			}
			finally {
				lock.readLock().unlock();
			}
		}

		public T get(long id) {
			lock.readLock().lock();
			try {
				return _get(id);
			}
			finally {
				lock.readLock().unlock();
			}
		}

		public List<T> getList(long[] ids) {
			lock.readLock().lock();
			try {
				List<T> result = new ArrayList<>();
				for (long id : ids) {
					result.add(_get(id));
				}
				return result;
			}
			finally {
				lock.readLock().unlock();
			}
		}

		public Set<T> getSet(long[] ids) {
			return new HashSet<>(getList(ids));
		}

		public long[] getIds(List<T> objects) {
			TLongList result = new TLongArrayList();
			for (T object : objects) {
				result.add(object.getId());
			}
			return result.toArray();
		}

		//Set

		public void cache() {
			List<T> list = getter.all();
			lock.writeLock().lock();
			try {
				_cache(list);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void update(T object) {
			lock.writeLock().lock();
			try {
				_update(object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		//Internals

		private T _get(long id) {
			if (BaseModel.isIdNull(id)) {
				return null;
			}
			return index.get(id);
		}

		protected void _cache(List<T> list) {
			index.clear();
			all.clear();
			for (T object : list) {
				_update(object);
			}
		}

		private void _update(T object) {
			index.put(object.getId(), object);
			all.add(object);
		}
	}

	public static class NamedIndex<T extends NamedModel> extends BaseIndex<T> {
		private Searchable<T> search;
		//Containers
		private Map<String, T> index;

		public NamedIndex(Getter<T> getter) {
			super(getter);
			this.index = new HashMap<>();
			this.search = new Searchable<>(this.index);
		}

		//Get

		public List<T> search(String query, int limit, boolean fullSearch) {
			lock.readLock().lock();
			try {
				List<T> searchResult = search.search(query, limit, fullSearch);
				List<T> result = new ArrayList<>();
				for (T object : searchResult) {
					if (object != null) {
						result.add(object);
					}
				}
				return result;
			}
			finally {
				lock.readLock().unlock();
			}
		}

		public T get(String name) {
			lock.readLock().lock();
			try {
				String key = _key(name);
				if (index.containsKey(key)) {
					return index.get(key);
				}
			}
			finally {
				lock.readLock().unlock();
			}
			return null;
		}

		//Set

		public void update(T object, String oldName) {
			String oldKey = _key(oldName);
			String newKey = _key(object);
			lock.writeLock().lock();
			try {
				if (oldKey != null && !Objects.equals(newKey, oldKey)) {
					index.remove(oldKey);
					search.reset();
				}
				update(object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		@Override
		public void update(T object) {
			String key = _key(object);
			lock.writeLock().lock();
			try {
				super.update(object);
				index.put(key, object);
				search.update(key);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void resetSearch() {
			lock.writeLock().lock();
			try {
				search.reset();
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		//Internals

		@Override
		protected void _cache(List<T> list) {
			super._cache(list);
			search.reset();
			index.clear();
			for (T object : list) {
				index.put(_key(object), object);
			}
		}

		protected String _key(T object) {
			return _key(object.getName());
		}

		protected String _key(String name) {
			if (name == null) {
				return null;
			}
			return name.toLowerCase();
		}
	}

	public static class ProductIndex extends NamedIndex<Product> {
		private TLongObjectMap<Map<String, Product>> products;

		public ProductIndex(Getter<Product> getter) {
			super(getter);
			this.products = new TLongObjectHashMap<>();
		}

		//Get

		public Product get(long brandId, String name) {
			String key = super._key(name);
			lock.readLock().lock();
			try {
				Map<String, Product> map = _getMap(brandId);
				if (map.containsKey(key)) {
					return map.get(key);
				}
			}
			finally {
				lock.readLock().unlock();
			}
			return null;
		}

		//Set

		public void update(Product object, long oldBrandId, String oldName) {
			String oldKey = super._key(oldName);
			String newKey = super._key(object.getName());
			lock.writeLock().lock();
			try {
				if ((!BaseModel.isIdNull(oldBrandId) && oldBrandId != object.getBrand_id()) ||
						(oldKey != null && !Objects.equals(oldKey, newKey))) {
					_getMap(oldBrandId).remove(oldKey);
				}
				super.update(object, oldName);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		@Override
		public void update(Product object) {
			String key = super._key(object.getName());
			lock.writeLock().lock();
			try {
				super.update(object);
				Map<String, Product> map = _getMap(object.getBrand_id());
				if (map != null) {
					map.put(key, object);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		//Internals

		@Override
		protected String _key(Product input) {
			return (input.getBrandName() + " " + input.getName()).toLowerCase();
		}

		@Override
		protected void _cache(List<Product> list) {
			super._cache(list);
			products.clear();
			Collection<Brand> brands = App.cache().brands.all();

			for (Brand brand : brands) {
				products.put(brand.getId(), new HashMap<>());
			}

			for (Product object : list) {
				Map<String, Product> map = _getMap(object.getBrand_id());
				if (map != null) {
					map.put(super._key(object), object);
				}
			}
		}

		private Map<String, Product> _getMap(long brandId) {
			if (BaseModel.isIdNull(brandId)) {
				return null;
			}
			Map<String, Product> map = products.get(brandId);
			if (map == null) {
				map = new HashMap<>();
				products.put(brandId, map);
			}
			return map;
		}
	}

	public static class OneToManyIndex<M extends BaseModel> {
		private ReadWriteLock lock;
		private Getter<M> getter;
		private OneToManyGetter<M> relation_getter;
		//Containers
		private TLongObjectMap<TLongList> index;

		public OneToManyIndex(OneToManyGetter<M> relation_getter, Getter<M> getter) {
			this.lock = new ReentrantReadWriteLock();
			this.relation_getter = relation_getter;
			this.getter = getter;
			this.index = new TLongObjectHashMap<>();
		}

		//Get

		public List<M> getManyObject(long one_id) {
			TLongList many_id_set = getMany(one_id);
			List<M> result = new ArrayList<>();
			for (long id : many_id_set.toArray()) {
				result.add(getter.byId(id));
			}
			return result;
		}

		public TLongList getMany(long one_id) {
			lock.writeLock().lock();
			try {
				return _getMany(one_id);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		//Set

		public void cache() {
			lock.writeLock().lock();
			try {
				_cache(getter.all());
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void add(M object) {
			add(object.getId(), relation_getter.getOneId(object));
		}

		public void remove(M object) {
			remove(object.getId(), relation_getter.getOneId(object));
		}

		public void add(long manyId, long oneId) {
			lock.writeLock().lock();
			try {
				_add(manyId, oneId);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void remove(long manyId, long oneId) {
			lock.writeLock().lock();
			try {
				_remove(manyId, oneId);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		//Internals

		protected void _cache(List<M> list) {
			index.clear();
			for (M object : list) {
				_add(object.getId(), relation_getter.getOneId(object));
			}
		}

		public void _add(long manyId, long oneId) {
			if (!BaseModel.isIdNull(oneId) && !BaseModel.isIdNull(manyId)) {
				_getMany(oneId).add(manyId);
			}
		}

		public void _remove(long manyId, long oneId) {
			if (!BaseModel.isIdNull(oneId) && !BaseModel.isIdNull(manyId)) {
				_getMany(oneId).remove(manyId);
			}
		}

		public TLongList _getMany(long oneId) {
			if (!index.containsKey(oneId)) {
				index.put(oneId, new TLongArrayList());
			}
			return index.get(oneId);
		}
	}

	public static class ManyToManyIndex<T extends ManyToMany> {
		private ReadWriteLock lock;
		private TLongObjectMap<Set<T>> left_index;
		private TLongObjectMap<Set<T>> right_index;
		private ManyToManyGetter<T> relation_getter;

		public ManyToManyIndex(ManyToManyGetter<T> relation_getter) {
			this.lock = new ReentrantReadWriteLock();
			this.relation_getter = relation_getter;
			this.left_index = new TLongObjectHashMap<>();
			this.right_index = new TLongObjectHashMap<>();
		}

		//Getters

		public Set<T> getR(long right_id) {
			if (!right_index.containsKey(right_id)) {
				right_index.put(right_id, new HashSet<>());
			}
			return right_index.get(right_id);
		}

		public Set<T> getL(long left_id) {
			if (!left_index.containsKey(left_id)) {
				left_index.put(left_id, new HashSet<>());
			}
			return left_index.get(left_id);
		}

		//Setters

		public void cache() {
			lock.writeLock().lock();
			try {
				_cache(relation_getter.all());
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void add(T object) {
			lock.writeLock().lock();
			try {
				_add(object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		public void remove(T object) {
			lock.writeLock().lock();
			try {
				_remove(object);
			}
			finally {
				lock.writeLock().unlock();
			}
		}

		//Internals

		private void _add(T object) {
			long left = object.getLeftId();
			long right = object.getRightId();
			getL(left).add(object);
			getR(right).add(object);
		}

		private void _remove(T object) {
			long left = object.getLeftId();
			long right = object.getRightId();
			getL(left).remove(object);
			getR(right).remove(object);
		}

		private void _cache(List<T> list) {
			lock.writeLock().lock();
			try {
				left_index.clear();
				right_index.clear();
				for (T object : list) {
					_add(object);
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}
	}

	//Getters

	public static interface Getter<T extends BaseModel> {
		public List<T> all();

		public T byId(Long id);
	}

	public static interface NamedGetter<T extends NamedModel> {
		public T byName(String name);
	}

	private static interface OneToManyGetter<M extends BaseModel> {
		public long getOneId(M object);
	}

	private static interface ManyToManyGetter<T extends ManyToMany> {
		public List<T> all();
	}

	private static class IngredientAliasGetter implements OneToManyGetter<Alias> {
		@Override
		public long getOneId(Alias object) {
			return object.getIngredient_id();
		}
	}

	private static class IngredientFunctionGetter implements ManyToManyGetter<IngredientFunction> {
		public List<IngredientFunction> all() {
			return IngredientFunction.all();
		}
	}

	private static class ProductIngredientGetter implements ManyToManyGetter<ProductIngredient> {
		public List<ProductIngredient> all() {
			return ProductIngredient.all();
		}
	}

	//Matching

	public static class Matcher {
		private ReadWriteLock lock;
		private MemCache cache;

		public Matcher(MemCache cache) {
			this.lock = new ReentrantReadWriteLock();
			this.cache = cache;
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

		public List<Alias> matchAllAliases(String input) {
			List<Alias> matches = new ArrayList<>();
			Set<Alias> matchSet = new HashSet<>();
			List<String> ingredients = splitIngredients(input);
			for (String ingredient : ingredients) {
				Alias name = matchAlias(ingredient);
				if (!matchSet.contains(name)) {
					matches.add(name);
					matchSet.add(name);
				}
			}
			return matches;
		}

		public Alias matchAlias(String input) {
			Alias name = App.cache().alias.get(input);
			if (name != null) {
				return name;
			}

			List<Alias> result = cache.alias.search(input, 1, true);
			if (!result.isEmpty()) {
				name = result.get(0);
			}

			return name;
		}
	}

	public static class Searchable<T> {
		private Map<String, T> names;
		private SearchEngine<T> search;

		public Searchable(Map<String, T> names) {
			this.names = names;
		}

		public List<T> search(String query, int limit, boolean fullSearch) {
			query = query.toLowerCase();
			SearchEngine<T> search_local;
			synchronized (this) {
				if (search == null) {
					search = new SearchEngine<>();
					search.init(names);
				}
				search_local = search;
			}
			List<T> result;
			if (fullSearch) {
				result = search_local.fullSearch(query, limit);
			}
			else {
				result = search_local.partialSearch(query, limit);
			}
			if (result == null) {
				result = new ArrayList<>();
			}
			return result;
		}

		public void update(String key) {
			SearchEngine<T> search_local;
			synchronized (this) {
				search_local = search;
			}
			if (search_local != null) {
				search_local.update(key);
			}
		}

		public void reset() {
			synchronized (this) {
				search = null;
			}
		}
	}

	//MemCache

	public NamedIndex<Function> functions;
	public NamedIndex<Brand> brands;
	public NamedIndex<ProductType> types;
	public NamedIndex<Ingredient> ingredients;
	public NamedIndex<Alias> alias;
	public ProductIndex products;

	//Relations
	public OneToManyIndex<Alias> ingredient_alias;
	public ManyToManyIndex<IngredientFunction> ingredient_function;
	public ManyToManyIndex<ProductIngredient> product_ingredient;

	//Others
	public Matcher matcher;

	public MemCache() {
		functions = new NamedIndex<>(Function.find);
		brands = new NamedIndex<>(Brand.find);
		types = new NamedIndex<>(ProductType.find);
		ingredients = new NamedIndex<>(Ingredient.find);
		alias = new NamedIndex<>(Alias.find);
		products = new ProductIndex(Product.find);

		ingredient_alias = new OneToManyIndex<>(new IngredientAliasGetter(), Alias.find);
		ingredient_function = new ManyToManyIndex<>(new IngredientFunctionGetter());
		product_ingredient = new ManyToManyIndex<>(new ProductIngredientGetter());

		matcher = new Matcher(this);
	}

	public void init() {
		functions.cache();
		brands.cache();
		types.cache();
		ingredients.cache();
		alias.cache();
		products.cache();

		ingredient_alias.cache();
		ingredient_function.cache();
		product_ingredient.cache();
		System.gc();
	}
}
