package controllers.api;

import api.API;
import api.UserAPI;
import api.request.BadRequestException;
import api.response.Response;
import controllers.session.SessionHelper;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {
	public static Result login() {
		Response response = null;
		try {
			UserAPI.RequestLogin request = API.readRequest(ctx(), UserAPI.RequestLogin.class);

			User user = User.byEmail(request.email);

			if (user.checkPassword(request.password)) {
				SessionHelper.setUser(session(), user);
				return ok();
			} else {
				return unauthorized();
			}
		}
		catch (BadRequestException e) {
			response = API.response(e);
			return API.writeResponse(ctx(), response);
		}
	}
}
