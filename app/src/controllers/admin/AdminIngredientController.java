package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.AdminUserApi;
import src.api.Api;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.Permissible;
import src.models.ingredient.Ingredient;

public class AdminIngredientController extends Controller {
	private static final String TAG = "AdminIngredientController";

	public static Result api_get_ingredient_by_id() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.DATA);

			AdminUserApi.RequestGetById request =
					Api.read(ctx(), AdminUserApi.RequestGetById.class);

			Ingredient result = Ingredient.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
