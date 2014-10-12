package src.controllers.admin;

import com.avaje.ebean.Ebean;
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
import src.controllers.api.response.ResponseMessage;
import src.controllers.data.IngredientController;
import src.controllers.util.ResponseState;
import src.models.Permissible;
import src.models.data.History;
import src.models.data.ingredient.AllIngredient;
import src.models.data.ingredient.Function;
import src.models.data.ingredient.Ingredient;
import src.models.user.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminIngredientController extends Controller {
	private static final String TAG = "AdminIngredientController";

	public static class RequestAllIngredientAddEdit extends Request {
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

	public static class ResponseAllIngredientObject {
		public long id;
		public String name;
		public String cas_number;
		public String description;
		public List<String> functions;
		public String submitted_by;
		public long submitted_time;
		public boolean approved;
		public long approved_time;
	}

	public static class ResponseAllIngredient extends Response {
		public List<ResponseAllIngredientObject> results;

		public ResponseAllIngredient() {
			this.results = new ArrayList<>();
		}
	}

	public static Result api_ingredient_by_id() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.VIEW);

			Api.RequestGetByIdAll request = Api.read(ctx(), Api.RequestGetByIdAll.class);

			List<AllIngredient> result = null;
			if (request.all) {
				result = AllIngredient.byTargetId(request.id);
			}
			else {
				result = AllIngredient.byApprovedTargetId(request.id);
			}

			ResponseAllIngredient response = new ResponseAllIngredient();

			for (AllIngredient ingredient : result) {
				ResponseAllIngredientObject object = new ResponseAllIngredientObject();

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

			RequestAllIngredientAddEdit request = Api.read(ctx(), RequestAllIngredientAddEdit.class);

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

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

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

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

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
