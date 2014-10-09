package src.controllers;

import src.api.API;
import src.api.UserAPI;
import src.api.request.BadRequestException;
import src.api.response.Response;
import src.api.response.ResponseMessage;
import src.controllers.session.SessionHelper;
import src.models.User;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {
	public static Result api_login() {
		Response response;
		try {
			UserAPI.RequestLogin request = API.readRequest(ctx(), UserAPI.RequestLogin.class);

			response = api_login(request);
		}
		catch (BadRequestException e) {
			response = new Response(e);
		}
		return API.writeResponse(response);
	}

	public static Response api_login(UserAPI.RequestLogin request) {
		User user = User.byEmail(request.email);

		if (user == null || !user.checkPassword(request.password)) {
			return new Response(Response.UNAUTHORIZED)
					.addMessage(ResponseMessage.error("Login failed"));
		}
		else {
			SessionHelper.setUser(session(), user);
			return new Response(Response.OK);
		}
	}
}
