package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.ErrorController;
import src.user.Permission;
import src.views.ResponseState;
import views.html.admin_home;

public class UserAdminController extends Controller {
	private static final String TAG = "UserAdminController";

	public static Result home() {
		ResponseState state = new ResponseState(session());

		if (!state.userHasPermission(Permission.ADMIN_VIEW)) {
			return ErrorController.notfound();
		}

		return ok(admin_home.render(null));
	}
}
