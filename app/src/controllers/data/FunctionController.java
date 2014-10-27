package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.MemCache;
import src.models.data.Function;
import src.models.user.Permissible;
import src.models.util.BaseModel;
import views.html.function;

import java.util.Collection;

public class FunctionController extends Controller {
	public static Result function(long function_id) {
		ResponseState state = new ResponseState(session());

		Function result = App.cache().functions.get(function_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(function.render(state, result));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_function_byid() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

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

			Api.ResponseNamedModel response = new Api.ResponseNamedModel(
					result.getId(),
					result.getName(),
					result.getDescription()
			);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_functions() {
		Collection<Function> result = App.cache().functions.all();

		Api.ResponseNamedModelList response = new Api.ResponseNamedModelList();

		for (Function object : result) {
			response.results.add(new Api.ResponseNamedModelObject(
					object.getId(),
					object.getName(),
					object.getDescription()
			));
		}

		return Api.write(response);
	}
}
