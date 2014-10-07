package controllers.api;

import api.API;
import api.IngredientAPI;
import api.response.Response;
import models.Ingredient;
import play.mvc.BodyParser;
import play.mvc.Result;

import static play.mvc.Controller.ctx;

public class IngredientController {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result info(long ingredient_id) {
		Ingredient ingredient = Ingredient.byId(ingredient_id);
		Response response = new IngredientAPI.ResponseIngredientInfo(ingredient.getName(),
				ingredient.getFunctionNames(),
				ingredient.getShort_desc());

		return API.writeResponse(ctx(), response);
	}
}