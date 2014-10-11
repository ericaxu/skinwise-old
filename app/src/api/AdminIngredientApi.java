package src.api;

import src.api.request.NotEmpty;
import src.api.request.NotNull;
import src.api.request.Request;
import src.api.response.Response;

import java.util.ArrayList;
import java.util.List;

public class AdminIngredientApi {
	public static class RequestAllIngredientAddEdit extends Request {
		public long id;
		@NotEmpty
		public String name;
		@NotEmpty
		public String cas_number;
		@NotEmpty
		public String description;
		@NotNull
		public List<String> functions;
	}

	public static class ResponseAllIngredientObject {
		public long id;
		public String name;
		public String cas_number;
		public String description;
		public List<String> functions;
		public String submitted_by;
		public long submitted_time;
		public boolean approved;
		public long approved_time;
	}

	public static class ResponseAllIngredient extends Response {
		public List<ResponseAllIngredientObject> results;

		public ResponseAllIngredient() {
			this.results = new ArrayList<>();
		}
	}
}
