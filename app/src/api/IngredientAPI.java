package src.api;

import src.api.request.NotEmpty;
import src.api.request.Request;
import src.api.response.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class IngredientAPI {
	public static class RequestIngredientInfo extends Request {
		@NotEmpty
		public long ingredient_id;

		public RequestIngredientInfo(@JsonProperty("ingredient_id") long ingredient_id) {
			this.ingredient_id = ingredient_id;
		}
	}

	public static class ResponseIngredientInfo extends Response {
		@NotEmpty
		public String ingredient_name;
		public List<String> functions;
		public String description;

		public ResponseIngredientInfo(@JsonProperty("ingredient_name") String ingredient_name,
		                              @JsonProperty("functions") List<String> functions,
		                              @JsonProperty("description") String description) {
			this.ingredient_name = ingredient_name;
			this.functions = functions;
			this.description = description;
		}
	}
}
