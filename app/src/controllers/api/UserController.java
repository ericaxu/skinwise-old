package src.controllers.api;

import src.api.UserAPI;
import src.api.request.BadRequestException;
import src.api.response.Response;
import src.controllers.session.SessionHelper;
import src.models.User;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {
	public static Result login() {
		Response response;
		try {
			UserAPI.RequestLogin request = API.readRequest(ctx(), UserAPI.RequestLogin.class);

			response = login(request);
		}
		catch (BadRequestException e) {
			response = new Response(e);
		}
		return API.writeResponse(response);
	}

	public static Response login(UserAPI.RequestLogin request) {
		User user = User.byEmail(request.email);

		if (user == null || !user.checkPassword(request.password)) {
			return new Response(Response.UNAUTHORIZED).setError("Login failed");
		}
		else {
			SessionHelper.setUser(session(), user);
			return new Response(Response.OK);
		}
	}
}
