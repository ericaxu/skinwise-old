package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.Api;
import src.api.response.Response;
import src.api.response.ResponseMessage;
import src.controllers.util.ResponseState;
import views.html.error404;

public class ErrorController extends Controller {
	public static Result notfound(String route) {
		return notfound();
	}

	public static Result notfound() {
		ResponseState state = new ResponseState(session());
		return ok(error404.render(state));
	}

	public static Result api_notfound(String route) {
		ResponseState state = new ResponseState(session());
		state.getResponse()
				.setCode(Response.NOT_FOUND)
				.addMessage(ResponseMessage.error("API Endpoint " + route + " not found"));
		return Api.write(state.getResponse());
	}
}
