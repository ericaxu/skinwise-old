package src.controllers.admin;

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
import src.models.BaseModel;
import src.models.MemCache;
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

	public static Result api_ingredient_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			RequestIngredientUpdate request = Api.read(ctx(), RequestIngredientUpdate.class);

			MemCache cache = App.cache();

			Ingredient result;
			if (request.id == BaseModel.NEW_ID) {
				result = new Ingredient();
			}
			else {
				result = cache.ingredients.get(request.id);
				if (result == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Ingredient " + request.id + " not found");
				}
			}

			Set<Function> functions = new HashSet<>();
			for (String f : request.functions) {
				Function function = cache.functions.get(f);
				if (function == null) {
					throw new BadRequestException(Response.INVALID, "Function " + f + " not found");
				}
				functions.add(function);
			}

			result.setCas_number(request.cas_number);
			result.setDescription(request.description);
			result.setFunctions(functions);

			cache.ingredients.updateNameAndSave(result, request.name);

			return Api.write(new InfoResponse("Ingredient " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_ingredient_name_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			RequestIngredientNameUpdate request = Api.read(ctx(), RequestIngredientNameUpdate.class);

			MemCache cache = App.cache();

			IngredientName result;
			if (request.id == BaseModel.NEW_ID) {
				result = new IngredientName();
			}
			else {
				result = cache.ingredient_names.get(request.id);
				if (result == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Ingredient Name " + request.id + " not found");
				}
			}

			Ingredient ingredient = cache.ingredients.get(request.ingredient_id);
			if (ingredient == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Ingredient " + request.ingredient_id + " not found");
			}

			result.setIngredient(ingredient);

			cache.ingredient_names.updateNameAndSave(result, request.name);

			return Api.write(new InfoResponse("Ingredient Name " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_function_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			Api.RequestObjectUpdate request = Api.read(ctx(), Api.RequestObjectUpdate.class);

			MemCache cache = App.cache();

			Function result;
			if (request.id == BaseModel.NEW_ID) {
				result = new Function();
			}
			else {
				result = cache.functions.get(request.id);
				if (result == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Function " + request.id + " not found");
				}
			}

			result.setDescription(request.description);

			cache.functions.updateNameAndSave(result, request.name);

			return Api.write(new InfoResponse("Function " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
