package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotEmpty;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.models.MemCache;
import src.models.data.Alias;
import src.models.data.Ingredient;
import src.models.data.Product;
import src.models.util.NamedModel;

import java.util.ArrayList;
import java.util.List;

public class DataController extends Controller {
	private static enum AutocompleteType {
		FUNCTION("function"),
		BENEFIT("benefit"),
		BRAND("brand"),
		TYPE("type"),
		INGREDIENT("ingredient"),
		ALIAS("alias"),
		PRODUCT("product");

		private String match;

		AutocompleteType(String match) {
			this.match = match;
		}

		public static AutocompleteType match(String match) {
			for (AutocompleteType type : AutocompleteType.values()) {
				if (type.match.equalsIgnoreCase(match)) {
					return type;
				}
			}
			return null;
		}
	}

	public static class RequestIngredientFilter extends Request {
		@NotEmpty
		public String query;
		@NotEmpty
		public String type;
	}

	public static class ResponseDataObject {
		public long id;
		public String name;

		public ResponseDataObject(long id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	public static class ResponseAutocomplete extends Response {
		public List<ResponseDataObject> results = new ArrayList<>();
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_autocomplete() {
		try {
			RequestIngredientFilter request = Api.read(ctx(), RequestIngredientFilter.class);

			AutocompleteType type = AutocompleteType.match(request.type);
			if (type == null) {
				throw new BadRequestException(Response.INVALID, "Unknown auto-complete type " + request.type);
			}

			ResponseAutocomplete response = new ResponseAutocomplete();
			int numResults = 10;
			MemCache cache = App.cache();

			List<? extends NamedModel> result = null;
			switch (type) {
				case FUNCTION:
					result = cache.functions.search(request.query, numResults, false);
					break;
				case BENEFIT:
					result = cache.benefits.search(request.query, numResults, false);
					break;
				case BRAND:
					result = cache.brands.search(request.query, numResults, false);
					break;
				case TYPE:
					result = cache.types.search(request.query, numResults, false);
					break;
				case INGREDIENT:
				case ALIAS:
					result = cache.alias.search(request.query, numResults, false);
					break;
				case PRODUCT:
					result = cache.products.search(request.query, numResults, false);
					break;
			}

			if (result != null) {
				for (NamedModel object : result) {
					ResponseDataObject responseDataObject = new ResponseDataObject(object.getId(), object.getName());

					//For aliases, we want ingredient ID instead, or skip if not associated
					if (type == AutocompleteType.INGREDIENT) {
						Ingredient ingredient = ((Alias) object).getIngredient();
						if (ingredient == null) {
							continue;
						}
						responseDataObject.id = ingredient.getId();
					}
					//For products, we want to display "Brand - Name"
					if (type == AutocompleteType.PRODUCT) {
						responseDataObject.name = ((Product) object).getBrandName() + " - " + responseDataObject.name;
					}

					response.results.add(responseDataObject);
				}
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result search(String query) {
		int numResults = 10;
		MemCache cache = App.cache();
		List<Alias> result = cache.alias.search(query, numResults, true);
		String finalResults = "";
		if (result != null) {
			for (NamedModel object : result) {
				finalResults += " \n<br> " + object.getName();
			}
		}

		return ok(finalResults);
	}
}