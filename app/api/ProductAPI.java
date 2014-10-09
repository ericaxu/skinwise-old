package api;

import api.request.Request;
import api.response.Response;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class ProductAPI {
	public static class RequestProductInfo extends Request {
		@NotEmpty
		public long product_id;

		public RequestProductInfo(@JsonProperty("product_id") long product_id) {
			this.product_id = product_id;
		}
	}

	public static class ResponseProductInfo extends Response {
		public String product_name;
		public String product_brand;
		public String description;

		public ResponseProductInfo(@JsonProperty("product_id") String product_name,
		                           @JsonProperty("product_brand") String product_brand,
		                           @JsonProperty("description") String description) {
			this.product_name = product_name;
			this.product_brand = product_brand;
			this.description = description;
		}
	}
}
