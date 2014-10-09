package src.controllers;

import src.api.response.Response;
import src.controllers.session.SessionHelper;
import src.user.Permission;
import src.models.User;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin_home;

public class AdminController extends Controller {
	public static Result home() {
		User user = SessionHelper.getUser(session());

		if (user == null || !user.hasPermission(Permission.ADMIN_ALL)) {
			return ErrorController.notfound();
		}

		return ok(admin_home.render(new Response().setError("TestError")));
	}

	public static Result auto_import() {
		User user = SessionHelper.getUser(session());

		if (user == null || !user.hasPermission(Permission.ADMIN_ALL)) {
			return ErrorController.notfound();
		}

		return ok(admin_home.render(null));
	}
}
