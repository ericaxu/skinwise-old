package controllers.api;

import api.API;
import api.UserAPI;
import api.request.BadRequestException;
import api.response.Response;
import api.response.ResponseStatus;
import controllers.session.SessionHelper;
import models.User;
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
			response = API.response(e);
		}
		return API.writeResponse(ctx(), response);
	}

	public static Response login(UserAPI.RequestLogin request) {
		User user = User.byEmail(request.email);

		if (user == null || !user.checkPassword(request.password)) {
			return API.response(ResponseStatus.UNAUTHORIZED);
		}
		else {
			SessionHelper.setUser(session(), user);
			return API.response(ResponseStatus.OK);
		}
	}
}
