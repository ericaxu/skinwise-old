package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotEmpty;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.SessionHelper;
import src.models.user.User;

public class UserController extends Controller {
	public transient static final String EMAIL_TAKEN = "EmailTaken";

	public static class RequestLogin extends Request {
		@NotEmpty
		public String email;
		@NotEmpty
		public String password;
	}

	public static class RequestSignup extends Request {
		@NotEmpty
		public String name;
		@NotEmpty
		public String email;
		@NotEmpty
		public String password;
	}

	public static Result api_login() {
		try {
			RequestLogin request = Api.read(ctx(), RequestLogin.class);

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
			RequestSignup request = Api.read(ctx(), RequestSignup.class);

			User user = User.byEmail(request.email);

			if (user != null) {
				throw new BadRequestException(EMAIL_TAKEN, "Email already taken");
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
