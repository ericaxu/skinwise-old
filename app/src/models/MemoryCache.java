package src.models;

import src.models.data.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryCache {
	public Map<Long, Function> functions = new HashMap<>();
	public Map<Long, Brand> brands = new HashMap<>();
	public Map<Long, ProductType> types = new HashMap<>();
	public Map<Long, Ingredient> ingredients = new HashMap<>();
	public Map<Long, IngredientName> ingredient_names = new HashMap<>();
	public Map<Long, Product> products = new HashMap<>();

	public Map<String, Function> function_index = new HashMap<>();
	public Map<String, Brand> brand_index = new HashMap<>();
	public Map<String, ProductType> type_index = new HashMap<>();
	public Map<String, Ingredient> ingredient_index = new HashMap<>();
	public Map<String, IngredientName> ingredient_name_index = new HashMap<>();
	public Map<String, Product> product_index = new HashMap<>();

	public void cacheFunctions() {
		List<Function> list = Function.all();
		cache(list, functions, function_index);
	}

	public void cacheBrands() {
		List<Brand> list = Brand.all();
		cache(list, brands, brand_index);
	}

	public void cacheTypes() {
		List<ProductType> list = ProductType.all();
		cache(list, types, type_index);
	}

	public void cacheIngredients() {
		List<Ingredient> list = Ingredient.all();
		cache(list, ingredients, ingredient_index);
	}

	public void cacheIngredientNames() {
		List<IngredientName> list = IngredientName.all();
		cache(list, ingredient_names, ingredient_name_index);
	}

	public void cacheProducts() {
		List<Product> list = Product.all();
		cache(list, products, product_index);
	}

	private <T extends NamedModel> void cache(List<T> list, Map<Long, T> map, Map<String, T> index) {
		for (T object : list) {
			map.put(object.getId(), object);
			index.put(object.getName().toLowerCase(), object);
		}
	}
}
