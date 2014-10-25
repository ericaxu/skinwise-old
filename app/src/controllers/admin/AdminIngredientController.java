package src.controllers.admin;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotEmpty;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.util.BaseModel;
import src.models.Permissible;
import src.models.data.Function;
import src.models.data.Ingredient;
import src.models.data.IngredientName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminIngredientController extends Controller {
	private static final String TAG = "AdminIngredientController";

	public static class RequestIngredientUpdate extends Request {
		public long id;
		@NotEmpty
		public String name;
		@NotEmpty
		public String cas_number;
		@NotNull
		public long popularity;
		@NotEmpty
		public String description;
		@NotNull
		public List<String> functions;
	}

	public static class RequestIngredientNameUpdate extends Request {
		public long id;
		public long ingredient_id;
		@NotEmpty
		public String name;
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_ingredient_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			RequestIngredientUpdate request = Api.read(ctx(), RequestIngredientUpdate.class);

			Ingredient result;
			if (request.id == BaseModel.NEW_ID) {
				result = new Ingredient();
			}
			else {
				result = App.cache().ingredients.get(request.id);
				if (result == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Ingredient " + request.id + " not found");
				}
			}

			Set<Function> functions = new HashSet<>();
			for (String f : request.functions) {
				Function function = App.cache().functions.get(f);
				if (function == null) {
					throw new BadRequestException(Response.INVALID, "Function " + f + " not found");
				}
				functions.add(function);
			}

			result.setCas_number(request.cas_number);
			result.setDescription(request.description);
			result.setPopularity(request.popularity);
			result.saveFunctions(functions);

			App.cache().ingredients.updateNameAndSave(result, request.name);

			return Api.write(new InfoResponse("Ingredient " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_ingredient_name_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			RequestIngredientNameUpdate request = Api.read(ctx(), RequestIngredientNameUpdate.class);

			IngredientName result;
			if (request.id == BaseModel.NEW_ID) {
				result = new IngredientName();
			}
			else {
				result = App.cache().ingredient_names.get(request.id);
				if (result == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Ingredient Name " + request.id + " not found");
				}
			}

			Ingredient ingredient;
			if (request.ingredient_id == BaseModel.NEW_ID) {
				ingredient = null;
			}
			else {
				ingredient = App.cache().ingredients.get(request.ingredient_id);
				if (ingredient == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Ingredient " + request.ingredient_id + " not found");
				}
			}

			result.setIngredient(ingredient);

			App.cache().ingredient_names.updateNameAndSave(result, request.name);

			return Api.write(new InfoResponse("Ingredient Name " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_function_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			Api.RequestObjectUpdate request = Api.read(ctx(), Api.RequestObjectUpdate.class);

			Function result;
			if (request.id == BaseModel.NEW_ID) {
				result = new Function();
			}
			else {
				result = App.cache().functions.get(request.id);
				if (result == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Function " + request.id + " not found");
				}
			}

			result.setDescription(request.description);

			App.cache().functions.updateNameAndSave(result, request.name);

			return Api.write(new InfoResponse("Function " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
