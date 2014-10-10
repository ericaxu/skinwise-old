package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.Api;
import src.api.UserApi;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.Response;
import src.controllers.util.SessionHelper;
import src.models.user.User;

public class UserController extends Controller {
	public static Result api_login() {
		try {
			UserApi.RequestLogin request = Api.read(ctx(), UserApi.RequestLogin.class);

			User user = User.byEmail(request.email);

			if (user == null || !user.checkPassword(request.password)) {
				throw new BadRequestException(Response.UNAUTHORIZED, "Login failed");
			}

			SessionHelper.setUser(session(), user);

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_signup() {
		try {
			UserApi.RequestSignup request = Api.read(ctx(), UserApi.RequestSignup.class);

			User user = User.byEmail(request.email);

			if (user != null) {
				throw new BadRequestException(UserApi.EMAIL_TAKEN, "Email already taken");
			}

			user = new User(request.email, request.password, request.name);
			user.save();

			SessionHelper.setUser(session(), user);

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_logout() {
		SessionHelper.setUser(session(), null);

		return Api.write();
	}
}
