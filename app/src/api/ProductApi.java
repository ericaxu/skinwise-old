package src.api;

import org.hibernate.validator.constraints.NotEmpty;
import src.api.request.Request;
import src.api.response.Response;

public class ProductApi {
	public static class ResponseProductInfo extends Response {
		public String product_name;
		public String product_brand;
		public String description;

		public ResponseProductInfo(String product_name, String product_brand, String description) {
			this.product_name = product_name;
			this.product_brand = product_brand;
			this.description = description;
		}
	}
}
