package src.controllers;

import src.api.API;
import src.api.IngredientAPI;
import src.api.response.Response;
import src.models.Ingredient;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class IngredientController extends Controller {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_info(long ingredient_id) {
		Ingredient ingredient = Ingredient.byId(ingredient_id);
		if (ingredient == null) {
			return API.writeResponse(new Response(Response.NOT_FOUND).setError("Product not found"));
		}

		Response response = new IngredientAPI.ResponseIngredientInfo(
				ingredient.getName(),
				ingredient.getFunctionNames(),
				ingredient.getDescription()
		);

		return API.writeResponse(response);
	}
}