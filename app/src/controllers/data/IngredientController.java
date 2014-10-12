package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.data.ingredient.Ingredient;
import views.html.*;

import java.util.List;

public class IngredientController extends Controller {
	public static class ResponseIngredientInfo extends Response {
		public long id;
		public String name;
		public String cas_number;
		public List<String> functions;
		public String description;

		public ResponseIngredientInfo(long id, String name, String cas_number, List<String> functions, String description) {
			this.id = id;
			this.name = name;
			this.cas_number = cas_number;
			this.functions = functions;
			this.description = description;
		}
	}

	public static Result info(long ingredient_id) {
		try {
			ResponseState state = new ResponseState(session());
			Ingredient result = Ingredient.byId(ingredient_id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Ingredient not found");
			}

			return ok(ingredient.render(state, result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_info() {
		try {
			Api.RequestGetById request =
					Api.read(ctx(), Api.RequestGetById.class);

			Ingredient result = Ingredient.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Ingredient not found");
			}

			Response response = new ResponseIngredientInfo(
					result.getId(),
					result.getName(),
					result.getCas_number(),
					result.getFunctionsString(),
					result.getDescription()
			);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}