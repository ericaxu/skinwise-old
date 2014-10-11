package src.controllers.admin;

import com.avaje.ebean.Ebean;
import play.mvc.Controller;
import play.mvc.Result;
import src.api.AdminIngredientApi;
import src.api.Api;
import src.api.GenericApi;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.InfoResponse;
import src.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.History;
import src.models.Permissible;
import src.models.ingredient.AllIngredient;
import src.models.ingredient.Function;
import src.models.ingredient.Ingredient;
import src.models.user.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminIngredientController extends Controller {
	private static final String TAG = "AdminIngredientController";

	public static Result api_ingredient_by_id() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.VIEW);

			GenericApi.RequestGetByIdAll request =
					Api.read(ctx(), GenericApi.RequestGetByIdAll.class);

			List<AllIngredient> result = null;
			if (request.all) {
				result = AllIngredient.byTargetId(request.id);
			}
			else {
				result = AllIngredient.byApprovedTargetId(request.id);
			}

			AdminIngredientApi.ResponseAllIngredient response =
					new AdminIngredientApi.ResponseAllIngredient();

			for (AllIngredient ingredient : result) {
				AdminIngredientApi.ResponseAllIngredientObject object =
						new AdminIngredientApi.ResponseAllIngredientObject();

				object.id = ingredient.getId();
				object.name = ingredient.getName();
				object.cas_number = ingredient.getCas_number();
				object.description = ingredient.getDescription();
				object.functions = ingredient.getFunctionsString();

				object.submitted_by = "";
				User user = User.byId(ingredient.getHistory().getSubmitted_by());
				if (user != null) {
					object.submitted_by = user.getEmail();
				}
				object.submitted_time = ingredient.getHistory().getSubmitted_time();
				object.approved = ingredient.getHistory().isApproved();
				object.approved_time = ingredient.getHistory().getApproved_time();
				response.results.add(object);
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_ingredient_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			AdminIngredientApi.RequestAllIngredientAddEdit request =
					Api.read(ctx(), AdminIngredientApi.RequestAllIngredientAddEdit.class);

			long target_id = History.TARGET_ID_NEW;
			if (request.id != target_id) {
				Ingredient target = Ingredient.byId(request.id);
				if (target == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
				}
				target_id = target.getId();
			}

			Set<Function> functions = new HashSet<>();
			for (String function : request.functions) {
				Function function1 = Function.byName(function.toLowerCase());
				if (function1 == null) {
					throw new BadRequestException(Response.INVALID, "Function " + function + " not found");
				}
				functions.add(function1);
			}

			User user = state.getUser();

			AllIngredient result = new AllIngredient(target_id, user.getId());

			result.setName(request.name);
			result.setCas_number(request.cas_number);
			result.setDescription(request.description);
			result.setFunctions(functions);

			result.save();

			return Api.write(new InfoResponse("Ingredient " + result.getName() + " updated, pending approval"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_ingredient_approve() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.APPROVE);

			GenericApi.RequestGetById request =
					Api.read(ctx(), GenericApi.RequestGetById.class);

			AllIngredient result = AllIngredient.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			result.approve();

			result.save();

			return Api.write(new InfoResponse("Ingredient " + result.getName() + " update approved"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_ingredient_delete() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.APPROVE);

			GenericApi.RequestGetById request =
					Api.read(ctx(), GenericApi.RequestGetById.class);

			Ingredient result = Ingredient.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			List<AllIngredient> all = AllIngredient.byTargetId(request.id);

			Ebean.beginTransaction();
			try {
				for (AllIngredient ingredient : all) {
					ingredient.delete();
				}

				result.delete();
				Ebean.commitTransaction();
			}
			finally {
				Ebean.endTransaction();
			}

			return Api.write(new InfoResponse("Successfully deleted ingredient " + request.id));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
