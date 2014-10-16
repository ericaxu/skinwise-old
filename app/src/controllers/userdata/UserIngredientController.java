package src.controllers.userdata;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.data.Ingredient;
import src.models.userdata.UserIngredients;

public class UserIngredientController extends Controller {
	public static class RequestAdd extends Request {
		@NotNull
		public String key;
		public long ingredient_id;
	}

	public static Result api_add() {
		ResponseState state = new ResponseState(session());

		try {
			if (state.getUser() == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Not logged in");
			}

			RequestAdd request = Api.read(ctx(), RequestAdd.class);

			Ingredient ingredient = Ingredient.byId(request.ingredient_id);
			if (ingredient == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Ingredient not found");
			}

			UserIngredients result = new UserIngredients();
			result.setUser(state.getUser());
			result.setKey(request.key);
			result.setIngredient(ingredient);
			result.save();

			return Api.write(new InfoResponse(""));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_remove() {
		ResponseState state = new ResponseState(session());

		try {
			if (state.getUser() == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Not logged in");
			}

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			UserIngredients result = UserIngredients.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Item not found");
			}

			if (!result.getUser().equals(state.getUser())) {
				throw new BadRequestException(Response.NOT_FOUND, "Item not found");
			}

			result.delete();

			return Api.write(new InfoResponse("Successfully deleted ingredient from list"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
