package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.Prettyfy;
import src.controllers.util.ResponseState;
import src.models.user.Permissible;
import src.util.Logger;
import views.html.error.error404;
import views.html.error.error500;

public class ErrorController extends Controller {
	public static Result notfound(String route) {
		return notfound();
	}

	public static Result notfound() {
		ResponseState state = new ResponseState(session());
		return ok(Prettyfy.prettify(error404.render(state)));
	}

	public static Result api_notfound(String route) {
		return Api.write(new ErrorResponse(Response.NOT_FOUND, "API Endpoint " + route + " not found"));
	}

	public static Result error(Throwable t) {
		ResponseState state = new ResponseState(session());

		Throwable result = null;

		if (state.userHasPermission(Permissible.ADMIN.ALL)) {
			result = t;
		}

		return ok(Prettyfy.prettify(error500.render(state, result)));
	}

	public static Result api_error(Throwable t) {
		ResponseState state = new ResponseState(session());
		if (state.userHasPermission(Permissible.ADMIN.ALL)) {
			String stacktrace = Logger.getStackTrace(t).replace("\n", "\n<br>");

			return Api.write(new ErrorResponse(Response.INTERNAL_ERROR,
					"A server error occured: " + t.getMessage() + " " + stacktrace));
		}

		return Api.write(new ErrorResponse(Response.INTERNAL_ERROR, "A server error occured"));
	}
}