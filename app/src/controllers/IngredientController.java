package src.controllers;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.api.Api;
import src.api.IngredientApi;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.Response;
import src.models.ingredient.Ingredient;

public class IngredientController extends Controller {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_info() {
		try {
			IngredientApi.RequestIngredientInfo request =
					Api.read(ctx(), IngredientApi.RequestIngredientInfo.class);

			Ingredient result = Ingredient.byId(request.ingredient_id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Ingredient not found");
			}

			Response response = new IngredientApi.ResponseIngredientInfo(
					result.getName(),
					result.getFunctionNames(),
					result.getDescription()
			);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}