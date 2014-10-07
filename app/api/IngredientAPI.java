package api;

import api.request.NotEmpty;
import api.request.Request;
import api.response.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

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
		public ArrayList<String> functions;
		public String description;

		public ResponseIngredientInfo(@JsonProperty("ingredient_name") String ingredient_name,
		                           @JsonProperty("functions") ArrayList<String> functions,
		                           @JsonProperty("description") String description) {
			this.ingredient_name = ingredient_name;
			this.functions = functions;
			this.description = description;
		}
	}
}
