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
import src.models.data.Ingredient;
import src.models.data.IngredientName;
import src.models.data.NamedModel;
import src.models.data.Product;

import java.util.ArrayList;
import java.util.List;

public class DataController extends Controller {
	private static enum AutocompleteType {
		FUNCTION("function"),
		BRAND("brand"),
		PRODUCT_TYPE("type"),
		INGREDIENT("ingredient"),
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
			MemCache cache = App.cache();

			List<? extends NamedModel> result = null;
			switch (type) {
				case FUNCTION:
					result = cache.functions.search(request.query, 20, false);
					break;
				case BRAND:
					result = cache.brands.search(request.query, 20, false);
					break;
				case PRODUCT_TYPE:
					result = cache.types.search(request.query, 20, false);
					break;
				case INGREDIENT:
					result = cache.ingredient_names.search(request.query, 20, false);
					break;
				case PRODUCT:
					result = cache.products.search(request.query, 20, false);
					break;
			}

			if (result != null) {
				for (NamedModel object : result) {
					long id = object.getId();
					String name = object.getName();

					//Ingredient names, we want ingredient ID instead, or skip if not associated
					if (object instanceof IngredientName) {
						Ingredient ingredient = ((IngredientName) object).getIngredient();
						if (ingredient == null) {
							continue;
						}
						id = ingredient.getId();
					}
					//Products, we want to display "Brand - Name"
					if (object instanceof Product) {
						name = ((Product) object).getBrandName() + " - " + name;
					}
					response.results.add(new ResponseDataObject(
							id, name
					));
				}
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}