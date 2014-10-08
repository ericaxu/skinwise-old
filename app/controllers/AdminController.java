package controllers;

import controllers.session.SessionHelper;
import models.Permission;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin_home;

public class AdminController extends Controller {
	public static Result home() {
		User user = SessionHelper.getUser(session());

		if (user == null || !user.hasPermission(Permission.ADMIN_ALL)) {
			return ErrorController.notfound();
		}

		return ok(admin_home.render());
	}
}
