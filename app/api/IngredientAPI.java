package api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class IngredientAPI {
	public static class RequestIngredientInfo extends Request {
		@NotEmpty
		public String ingredient_id;

		public RequestIngredientInfo(@JsonProperty("ingredient_id") String ingredient_id) {
			this.ingredient_id = ingredient_id;
		}
	}

	public static class ResponseIngredientInfo extends Response {
		@NotEmpty
		public String ingredient_name;
		public String[] functions;
		public String description;

		public ResponseIngredientInfo(@JsonProperty("ingredient_name") String ingredient_name,
		                           @JsonProperty("functions") String[] functions,
		                           @JsonProperty("description") String description) {
			this.ingredient_name = ingredient_name;
			this.functions = functions;
			this.description = description;
		}
	}
}
