package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.UserAPI;
import src.api.request.BadRequestException;
import src.api.response.Response;
import src.api.response.ResponseMessage;
import src.controllers.session.SessionHelper;
import src.models.User;

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

	public static Result api_signup() {
		Response response;
		try {
			UserAPI.RequestSignup request = API.readRequest(ctx(), UserAPI.RequestSignup.class);

			response = api_signup(request);
		}
		catch (BadRequestException e) {
			response = new Response(e);
		}
		return API.writeResponse(response);
	}

	public static Response api_signup(UserAPI.RequestSignup request) {
		User user =  User.byEmail(request.email);

		if (user != null) {
			return new Response().addMessage(ResponseMessage.error("Email already taken"));
		}

		else {
			user = new User(request.email, request.password, request.name);
			user.save();
			SessionHelper.setUser(session(), user);
			return new Response(Response.OK);
		}
	}
}
