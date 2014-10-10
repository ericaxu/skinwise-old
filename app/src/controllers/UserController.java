package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.UserApi;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.Response;
import src.controllers.session.SessionHelper;
import src.models.User;

public class UserController extends Controller {
	public static Result api_login() {
		try {
			UserApi.RequestLogin request = API.read(ctx(), UserApi.RequestLogin.class);

			User user = User.byEmail(request.email);

			if (user == null || !user.checkPassword(request.password)) {
				throw new BadRequestException(Response.UNAUTHORIZED, "Login failed");
			}

			SessionHelper.setUser(session(), user);

			return API.write();
		}
		catch (BadRequestException e) {
			return API.write(new ErrorResponse(e));
		}
	}

	public static Result api_signup() {
		try {
			UserApi.RequestSignup request = API.read(ctx(), UserApi.RequestSignup.class);

			User user = User.byEmail(request.email);

			if (user != null) {
				throw new BadRequestException(UserApi.EMAIL_TAKEN, "Email already taken");
			}

			user = new User(request.email, request.password, request.name);
			user.save();

			SessionHelper.setUser(session(), user);

			return API.write();
		}
		catch (BadRequestException e) {
			return API.write(new ErrorResponse(e));
		}
	}

	public static Result api_logout() {
		SessionHelper.setUser(session(), null);

		return API.write();
	}
}
