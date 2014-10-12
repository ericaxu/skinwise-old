package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
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
import src.models.Permissible;
import src.models.data.Function;
import src.models.data.Ingredient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminIngredientController extends Controller {
	private static final String TAG = "AdminIngredientController";

	public static class RequestIngredientAddEdit extends Request {
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

	public static Result api_ingredient_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			RequestIngredientAddEdit request = Api.read(ctx(), RequestIngredientAddEdit.class);

			Ingredient result = Ingredient.byId(request.id);
			if (request.id == BaseModel.NEW_ID) {
				result = new Ingredient();
			}
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			Set<Function> functions = new HashSet<>();
			for (String function : request.functions) {
				Function function1 = Function.byName(function.toLowerCase());
				if (function1 == null) {
					throw new BadRequestException(Response.INVALID, "Function " + function + " not found");
				}
				functions.add(function1);
			}

			result.setName(request.name);
			result.setCas_number(request.cas_number);
			result.setDescription(request.description);
			result.setFunctions(functions);

			result.save();

			return Api.write(new InfoResponse("Ingredient " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_ingredient_delete() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.APPROVE);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Ingredient result = Ingredient.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			//TODO Mark deleted
			result.delete();

			return Api.write(new InfoResponse("Successfully deleted ingredient " + request.id));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
