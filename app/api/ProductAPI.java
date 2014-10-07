package api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class ProductAPI {
	public static class RequestProductInfo extends Request {
		@NotEmpty
		public String product_id;

		public RequestProductInfo(@JsonProperty("product_id") String product_id) {
			this.product_id = product_id;
		}
	}

	public static class ResponseProductInfo extends Response {
		@NotEmpty
		public String product_name;
		public String product_brand;
		public String[] ingredients;
		public String description;

		public ResponseProductInfo(@JsonProperty("product_id") String product_name,
		                           @JsonProperty("product_brand") String product_brand,
		                           @JsonProperty("functions") String[] ingredients,
		                           @JsonProperty("description") String description) {
			this.product_name = product_name;
			this.product_brand = product_brand;
			this.ingredients = ingredients;
			this.description = description;
		}
	}
}
