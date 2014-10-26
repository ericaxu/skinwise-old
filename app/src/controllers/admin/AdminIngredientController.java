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
import src.models.data.Alias;
import src.models.data.Function;
import src.models.data.Ingredient;
import src.models.user.Permissible;
import src.models.util.BaseModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminIngredientController extends Controller {
	private static final String TAG = "AdminIngredientController";

	public static class RequestIngredientUpdate extends Request {
		public long id;
		public long popularity;
		@NotEmpty
		public String name;
		@NotNull
		public String cas_number;
		@NotNull
		public String description;
		@NotNull
		public List<String> functions;
	}

	public static class RequestAliasUpdate extends Request {
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

			String oldName = result.getName();

			synchronized (result) {
				result.setName(request.name);
				result.setCas_number(request.cas_number);
				result.setDescription(request.description);
				result.setPopularity(request.popularity);
				result.setFunctions(functions);
				result.save();

				App.cache().ingredients.update(result, oldName);
			}

			return Api.write(new InfoResponse("Ingredient " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_alias_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			RequestAliasUpdate request = Api.read(ctx(), RequestAliasUpdate.class);

			Alias result;
			if (request.id == BaseModel.NEW_ID) {
				result = new Alias();
			}
			else {
				result = App.cache().alias.get(request.id);
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

			String oldName = result.getName();

			synchronized (result) {
				result.setName(request.name);
				result.setIngredient(ingredient);
				result.save();

				App.cache().alias.update(result, oldName);
			}

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
			String oldName = result.getName();

			synchronized (result) {
				result.setName(request.name);
				result.setDescription(request.description);
				result.save();

				App.cache().functions.update(result, oldName);
			}

			return Api.write(new InfoResponse("Function " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
