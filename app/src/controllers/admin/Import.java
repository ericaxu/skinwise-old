package src.controllers.admin;

import src.App;
import src.models.MemCache;
import src.models.data.*;
import src.models.util.BaseModel;
import src.models.util.NamedModel;
import src.util.JoinableExecutor;
import src.util.Json;
import src.util.Logger;
import src.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Import {
	private static final String TAG = "Import";
	private static final String percentageRegex = "\\(*\\s*([0-9\\.]+)\\s*%\\s*\\)*";
	private static final Pattern percentagePattern = Pattern.compile(percentageRegex);
	private static final Pattern spfPattern = Pattern.compile("(?i)SPF *([0-9]+)");

	public static synchronized void importDB(String path) throws IOException {
		String json = Util.readAll(path);
		DBFormat input = Json.deserialize(json, DBFormat.class);
		MemCache cache = App.cache();

		Logger.debug(TAG, "Importing functions");
		for (DBFormat.NamedObject object : sortNamedObjects(input.functions)) {
			object.sanitize();
			createFunction(object, cache);
		}

		Logger.debug(TAG, "Importing benefits");
		for (DBFormat.NamedObject object : sortNamedObjects(input.benefits)) {
			object.sanitize();
			createBenefit(object, cache);
		}

		Logger.debug(TAG, "Importing brands");
		for (DBFormat.NamedObject object : sortNamedObjects(input.brands)) {
			object.sanitize();
			createBrand(object, cache);
		}

		Logger.debug(TAG, "Importing types");
		for (DBFormat.TypeOject object : sortNamedObjects(input.types)) {
			object.sanitize();
			createType(object, cache);
		}
		for (DBFormat.TypeOject object : input.types.values()) {
			createTypeWithParent(object, cache);
		}

		Logger.debug(TAG, "Importing ingredients");
		for (DBFormat.IngredientObject object : input.ingredients.values()) {
			object.sanitize();
			createIngredient(object, cache);
		}

		Logger.debug(TAG, "Looking through products");

		Set<String> allIngredients = new HashSet<>();
		Set<String> brands = new HashSet<>();
		Set<String> types = new HashSet<>();
		for (DBFormat.ProductObject object : input.products.values()) {
			object.sanitize();

			brands.add(object.brand);
			types.addAll(Arrays.asList(object.types.split(",")));

			List<String> ingredients = MemCache.Matcher.splitIngredients(object.ingredients);
			allIngredients.addAll(ingredients);
			ingredients = MemCache.Matcher.splitIngredients(object.key_ingredients);
			allIngredients.addAll(ingredients);
		}

		brands.remove("");
		types.remove("");
		allIngredients.remove("");

		Set<String> allIngredientsNew = new HashSet<>();
		for (String ingredient : allIngredients) {
			allIngredientsNew.add(Util.cleanTrim(ingredient.replaceAll("\\(*\\s*[0-9\\.]+\\s*%\\s*\\)*", "")));
		}

		allIngredients = allIngredientsNew;

		Logger.debug(TAG, "Importing product brands and types");
		//Create brands not entered in the system
		for (String brand : brands) {
			Brand object = cache.brands.get(brand);
			if (object == null) {
				object = new Brand();
				object.setName(brand);
				object.setDescription("");
				object.save();
				cache.brands.update(object);
			}
		}

		//Create types not entered in the system
		for (String type : types) {
			Type object = cache.types.get(type);
			if (object == null) {
				object = new Type();
				object.setName(type);
				object.setDescription("");
				object.save();
				cache.types.update(object);
			}
		}

		Logger.debug(TAG, "Importing product ingredients");
		List<Alias> pending;

		//First pass
		Logger.debug(TAG, "Ingredient names - first pass");
		pending = multithreadedIngredientSearch(allIngredients);
		for (Alias name : pending) {
			name.save();
			cache.alias.update(name);
		}

		//Second pass
		Logger.debug(TAG, "Ingredient names - second pass");
		pending = multithreadedIngredientSearch(allIngredients);
		for (Alias name : pending) {
			name.save();
			cache.alias.update(name);
		}

		//Unmatched
		int matched = allIngredients.size();
		for (String string : allIngredients) {
			Alias alias = cache.matcher.matchAlias(string);

			if (alias == null) {
				alias = new Alias();
				alias.setName(string);
				alias.save();
				cache.alias.update(alias);
				matched--;
			}
		}

		Logger.debug(TAG, "Matched " + matched + "/" + allIngredients.size() + " ingredients from all products");

		Logger.debug(TAG, "Importing products");
		for (DBFormat.ProductObject object : sortNamedObjects(input.products)) {
			createProduct(object, cache);
		}
	}

	private static <T extends DBFormat.NamedObject> List<T> sortNamedObjects(Map<String, T> input) {
		List<T> result = new ArrayList<>(input.values());
		result.sort(new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				boolean a1 = BaseModel.isIdNull(o1.id);
				boolean a2 = BaseModel.isIdNull(o2.id);
				if (a1 == a2) {
					return Long.compare(o1.id, o2.id);
				}
				if (a1) {
					return 1;
				}
				else {
					return -1;
				}
			}
		});
		return result;
	}

	private static <T extends NamedModel> void createNamedObject(DBFormat.NamedObject object,
	                                                             MemCache.NamedIndex<T> index, T result) {
		if (!BaseModel.isIdNull(object.id)) {
			long old_id = result.getId();
			if (old_id != object.id) {
				if (BaseModel.isIdNull(old_id)) {
					result.setId(object.id);
				}
				else {
					Logger.info(TAG, "Id mismatch! Trying to substitute " + object.id + " for " + old_id);
				}
			}
		}

		result.setName(object.name);
		result.setDescription(object.description);

		result.save();

		index.update(result);
	}

	private static void createFunction(DBFormat.NamedObject object, MemCache cache) {
		Function result = cache.functions.get(object.name);

		if (result == null) {
			result = new Function();
		}

		createNamedObject(object, cache.functions, result);
	}

	private static void createBenefit(DBFormat.NamedObject object, MemCache cache) {
		Benefit result = cache.benefits.get(object.name);

		if (result == null) {
			result = new Benefit();
		}

		createNamedObject(object, cache.benefits, result);
	}

	private static void createBrand(DBFormat.NamedObject object, MemCache cache) {
		Brand result = cache.brands.get(object.name);

		if (result == null) {
			result = new Brand();
		}

		createNamedObject(object, cache.brands, result);
	}

	private static void createType(DBFormat.TypeOject object, MemCache cache) {
		Type result = cache.types.get(object.name);

		if (result == null) {
			result = new Type();
		}

		createNamedObject(object, cache.types, result);
	}

	private static void createTypeWithParent(DBFormat.TypeOject object, MemCache cache) {
		Type parent = cache.types.get(object.parent);
		if (parent == null && !object.parent.isEmpty()) {
			Logger.info(TAG, "Type parent " + object.parent + " not found!");
		}

		Type result = cache.types.get(object.name);
		if (result == null) {
			Logger.info(TAG, "Type " + object.name + " not found!");
			return;
		}

		synchronized (result) {
			result.setParent(parent);
			result.save();
		}
	}

	private static void createIngredient(DBFormat.IngredientObject object, MemCache cache) {
		//Remove duplicates
		for (String name : object.alias) {
			Alias alias = cache.alias.get(name);
			if (alias == null) {
				continue;
			}
			Ingredient ingredient = alias.getIngredient();
			if (ingredient != null && !Objects.equals(ingredient.getName(), object.name)) {
				//Logger.debug(TAG, "Duplicate ingredient " +
				//		object.name + " | " + ingredient.getName());

				//Attach alt names to ingredient
				for (String name2 : object.alias) {
					createAlias(name2, ingredient, cache);
				}
				//Attach info if possible
				if (ingredient.getCas_number().isEmpty()) {
					ingredient.setCas_number(object.cas_no);
				}
				if (ingredient.getDescription().isEmpty()) {
					ingredient.setDescription(object.description);
				}
				ingredient.save();
				return;
			}
		}

		Set<Function> functions = new HashSet<>();
		for (String f : object.functions) {
			f = f.trim();
			if (f.isEmpty()) {
				continue;
			}
			Function function = cache.functions.get(f);
			if (function == null) {
				Logger.debug(TAG, "Function not found! " + f);
			}
			else {
				functions.add(function);
			}
		}

		Set<Benefit> benefits = new HashSet<>();
		for (String b : object.benefits) {
			b = b.trim();
			if (b.isEmpty()) {
				continue;
			}
			Benefit benefit = cache.benefits.get(b);
			if (benefit == null) {
				Logger.debug(TAG, "Benefit not found! " + b);
			}
			else {
				benefits.add(benefit);
			}
		}

		Ingredient result = cache.ingredients.get(object.name);

		if (result == null) {
			result = new Ingredient();
		}

		if (object.display_name.isEmpty()) {
			object.display_name = object.name;
		}
		result.setDisplay_name(object.display_name);
		result.setCas_number(object.cas_no);
		result.setActive(object.active);
		if (object.popularity != 0) {
			result.setPopularity(object.popularity);
		}
		result.setFunctions(functions);
		result.setBenefits(benefits);

		createNamedObject(object, cache.ingredients, result);

		createAlias(object.name, result, cache);

		for (String name : object.alias) {
			createAlias(name, result, cache);
		}
	}

	private static void createAlias(String alias, Ingredient ingredient, MemCache cache) {
		Alias result = cache.alias.get(alias);
		if (result == null) {
			result = new Alias();
		}

		Ingredient old = result.getIngredient();
		if (old != null && !old.equals(ingredient)) {
			Logger.debug(TAG, alias + " attached to multiple ingredients " +
					ingredient.getName() + " | " + old.getName());
		}

		result.setName(alias);
		result.setIngredient(ingredient);
		result.save();

		cache.alias.update(result);
	}

	private static void createProduct(DBFormat.ProductObject object, MemCache cache) {
		List<String> ingredientOriginals = new ArrayList<>();
		List<String> keyIngredientOriginals = new ArrayList<>();
		List<Alias> ingredients = cache.matcher.matchAllAliases(object.ingredients, ingredientOriginals);
		List<Alias> key_ingredients = cache.matcher.matchAllAliases(object.key_ingredients, keyIngredientOriginals);

		Set<Ingredient> keyIngredientsSet = new HashSet<>();
		for (Alias alias : key_ingredients) {
			Ingredient ingredient = alias.getIngredient();
			if (ingredient != null) {
				keyIngredientsSet.add(ingredient);
			}
		}

		Iterator<Alias> iterator = ingredients.iterator();
		Iterator<String> iterator2 = ingredientOriginals.iterator();
		while (iterator.hasNext()) {
			Alias alias = iterator.next();
			String original = iterator2.next();
			Ingredient ingredient = alias.getIngredient();
			if (ingredient != null && ingredient.isActive()) {
				key_ingredients.add(alias);
				keyIngredientOriginals.add(original);
				keyIngredientsSet.add(ingredient);
			}

			if (keyIngredientsSet.contains(ingredient)) {
				iterator.remove();
				iterator2.remove();
			}
		}

		List<ProductProperty> properties = new ArrayList<>();

		int i = -1;
		for (Alias alias : key_ingredients) {
			i++;
			long ingredient_id = alias.getIngredient_id();
			if (BaseModel.isIdNull(ingredient_id)) {
				continue;
			}
			String original = keyIngredientOriginals.get(i);
			Matcher percentageMatcher = percentagePattern.matcher(original);
			if (percentageMatcher.find()) {
				double percent = Util.getNumberFrom(percentageMatcher, 1);
				String key = String.format(ProductProperty.INGREDIENT_PERCENT, ingredient_id);
				properties.add(createProperty(key, percent, null));
			}
		}

		Matcher spfMatcher = spfPattern.matcher(object.name);
		if (spfMatcher.find()) {
			double spf = Util.getNumberFrom(spfMatcher, 1);
			properties.add(createProperty(ProductProperty.SUNSCREEN_SPF, spf, null));
		}

		Brand brand = cache.brands.get(object.brand);
		String[] typesSting = object.types.split(",");
		Set<Type> types = new HashSet<>();
		for (String typeString : typesSting) {
			Type type = cache.types.get(typeString);
			if (type != null) {
				types.add(type);
			}
		}

		Product result = cache.products.get(brand.getId(), object.name);
		if (result == null) {
			result = new Product();
		}

		String oldName = result.getName();
		long oldBrandId = result.getBrand_id();
		result.setBrand(brand);
		result.setTypes(types);
		result.setImage(object.image);
		long price = parsePrice(object.price);
		if (!object.price.isEmpty()) {
			properties.add(createProperty(ProductProperty.PRICE, price, Util.formatPrice(price)));
		}
		if (!object.size.isEmpty() && object.size.contains(" ")) {
			String[] pieces = object.size.split(" ");
			String sizeString = pieces[0];
			String unit = object.size.substring(sizeString.length() + 1);
			float size = Float.parseFloat(sizeString);
			properties.add(createProperty(ProductProperty.SIZE, size, unit));
			if (!object.price.isEmpty()) {
				properties.add(createProperty(ProductProperty.PRICE_PER_SIZE, (double) price / size, null));
			}
		}
		if (object.popularity != 0) {
			result.setPopularity(object.popularity);
		}
		result.setIngredients(ingredients);
		result.setKeyIngredients(key_ingredients);

		createNamedObject(object, cache.products, result);

		cache.products.update(result, oldBrandId, oldName);

		for (ProductProperty property : properties) {
			property.setProduct_id(result.getId());
			property.save();
			cache.product_properties.update(property);
		}
	}

	private static ProductProperty createProperty(String key, double numberValue, String textValue) {
		ProductProperty property = new ProductProperty();
		property.setKey(key);
		property.setNumber_value(numberValue);
		property.setText_value(textValue);
		return property;
	}

	private static List<Alias> multithreadedIngredientSearch(Set<String> ingredients) {
		ConcurrentMap<String, Alias> results = new ConcurrentHashMap<>();

		int threads = Runtime.getRuntime().availableProcessors();
		Logger.debug(TAG, "Using " + threads + " threads to match ingredients");
		JoinableExecutor executor = new JoinableExecutor(Executors.newFixedThreadPool(threads));

		for (String name : ingredients) {
			executor.execute(new IngredientSearch(name, results));
		}

		try {
			executor.join();
		}
		catch (InterruptedException e) {
			Logger.error(TAG, e);
		}

		return new ArrayList<>(results.values());
	}

	private static long parsePrice(String price) {
		try {
			return Util.parsePrice(price);
		}
		catch (NumberFormatException e) {
			Logger.info(TAG, "Bad price: " + price);
			return 0;
		}
	}

	private static class IngredientSearch implements Runnable {
		private String name;
		private ConcurrentMap<String, Alias> results;

		public IngredientSearch(String name, ConcurrentMap<String, Alias> results) {
			this.name = name;
			this.results = results;
		}

		@Override
		public void run() {
			Alias alias = App.cache().matcher.matchAlias(name);
			if (alias != null && !alias.getName().equalsIgnoreCase(name)) {
				Ingredient ingredient = alias.getIngredient();
				if (ingredient != null) {
					alias = new Alias();
					alias.setName(name);
					alias.setIngredient(ingredient);
					results.putIfAbsent(name, alias);
				}
			}
		}
	}
}
