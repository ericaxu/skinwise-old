package src.util.dbimport;

import src.util.Json;

import java.io.IOException;

public class ImportProducts {
	private static final String TAG = "ProductImport";

	public static void importDB(String json) throws IOException {
		ProductFormat result = Json.deserialize(json, ProductFormat.class);

		//Import ingredients
		for (ProductObject object : result.products) {
			createOrUpdate(object);
		}
	}

	private static void createOrUpdate(ProductObject object) {

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
