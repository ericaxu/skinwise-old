package src.util.dbimport;

import src.models.data.History;
import src.models.data.product.AllProduct;
import src.models.data.product.Product;
import src.util.Json;
import src.util.Util;

import java.io.IOException;

public class ImportProducts {
	private static final String TAG = "ImportProducts";

	public static synchronized void importDB(String path) throws IOException {
		String json = Util.readAll(path);
		ProductFormat result = Json.deserialize(json, ProductFormat.class);

		//Import products
		for (ProductObject object : result.products) {
			createOrUpdate(object);
		}
	}

	private static void createOrUpdate(ProductObject object) {
		object.name = Util.notNull(object.name).toLowerCase();
		object.brand = Util.notNull(object.brand);
		object.claims = Util.notNull(object.claims);
		object.ingredients = Util.notNull(object.ingredients);

		Product target = Product.byBrandAndName(object.brand, object.name);
		long target_id = History.getTargetId(target);

		AllProduct result = new AllProduct(target_id, History.SUBMITTED_BY_SYSTEM);
		result.setName(object.name);
		result.setBrand(object.brand);
		result.setDescription(object.claims);
		result.setIngredients(object.ingredients);

		result.save();
		result.approve();
	}

	public static class ProductFormat {
		public ProductObject[] products;
	}

	public static class ProductObject {
		public String name;
		public String brand;
		public String claims;
		public String ingredients;
	}
}
