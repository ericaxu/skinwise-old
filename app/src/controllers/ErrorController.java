package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.response.Response;
import src.api.response.ResponseMessage;
import src.views.RenderState;
import views.html.error404;

public class ErrorController extends Controller {
	public static Result notfound() {
		RenderState state = new RenderState(session());
		return ok(error404.render(state));
	}

	public static Result api_notfound(String route) {
		Response response = new Response(Response.NOT_FOUND)
				.addMessage(ResponseMessage.error("API Endpoint " + route + " not found"));
		return API.writeResponse(response);
	}
}
