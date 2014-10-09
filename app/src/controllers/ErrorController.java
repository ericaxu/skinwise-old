package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.response.Response;
import src.api.response.ResponseMessage;
import views.html.error404;

public class ErrorController extends Controller {
	public static Result notfound() {
		return ok(error404.render(null));
	}

	public static Result api_notfound(String route) {
		Response response = new Response(Response.NOT_FOUND)
				.addMessage(ResponseMessage.error("API Endpoint " + route + " not found"));
		return API.writeResponse(response);
	}
}
