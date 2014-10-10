package src.api;

import src.api.request.NotEmpty;
import src.api.request.Request;
import src.api.response.Response;

import java.util.List;

public class IngredientAPI {
	public static class RequestIngredientInfo extends Request {
		@NotEmpty
		public long ingredient_id;
	}

	public static class ResponseIngredientInfo extends Response {
		public String ingredient_name;
		public List<String> functions;
		public String description;

		public ResponseIngredientInfo(String ingredient_name, List<String> functions, String description) {
			this.ingredient_name = ingredient_name;
			this.functions = functions;
			this.description = description;
		}
	}
}
