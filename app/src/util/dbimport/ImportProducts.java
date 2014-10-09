package src.util.dbimport;

import src.util.Json;
import src.util.Util;

import java.io.IOException;

public class ImportProducts {
	private static final String TAG = "ProductImport";

	public static void importDB(String json) throws IOException {
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

		/*Product product = Product.byNameAndBrand(object.name, object.brand);
		if (product == null) {
			product = new Product(object.name, object.brand, object.claims);
		}
*/
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
