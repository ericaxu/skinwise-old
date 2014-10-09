package controllers.api;

import api.IngredientAPI;
import api.response.Response;
import models.Ingredient;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class IngredientController extends Controller {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result info(long ingredient_id) {
		Ingredient ingredient = Ingredient.byId(ingredient_id);
		if (ingredient == null) {
			return API.writeResponse(new Response(Response.OK).setError("Product not found"));
		}

		Response response = new IngredientAPI.ResponseIngredientInfo(
				ingredient.getName(),
				ingredient.getFunctionNames(),
				ingredient.getShort_desc()
		);

		return API.writeResponse(response);
	}
}