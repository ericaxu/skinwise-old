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
import src.models.data.IngredientName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataController extends Controller {
	private static final String TYPE_INGREDIENT = "ingredient";

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

			ResponseAutocomplete response = new ResponseAutocomplete();
			MemCache cache = App.cache();

			if (Objects.equals(TYPE_INGREDIENT, request.type)) {
				List<IngredientName> result = cache.ingredient_names.search(request.query, 20, false);
				for (IngredientName object : result) {
					response.results.add(new ResponseDataObject(
							object.getId(), object.getName()
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