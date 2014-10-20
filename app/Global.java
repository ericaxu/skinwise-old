import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import src.controllers.ErrorController;
import src.models.Permissible;
import src.models.user.User;
import src.models.user.Usergroup;
import src.util.Logger;

public class Global extends GlobalSettings {
	private static final String TAG = "Global";
	public static final String ADMIN_USER_GROUP = "Administrators";

	@Override
	public void onStart(Application app) {
		Usergroup admin_group = Usergroup.byName(ADMIN_USER_GROUP);
		if (admin_group == null) {
			admin_group = new Usergroup();
			admin_group.setName(ADMIN_USER_GROUP);
			admin_group.addPermission(Permissible.ALL);
			admin_group.save();
		}

		String admin_email = "admin@skinwise.com";
		String admin_pass = "test@123?!";
		User admin = User.byEmail(admin_email);
		if (admin == null) {
			admin = new User(admin_email, admin_pass, "Admin");
			admin.setGroup(admin_group);
			admin.save();
		}
	}

	@Override
	public void onStop(Application app) {
	}

	@Override
	public F.Promise<Result> onHandlerNotFound(Http.RequestHeader requestHeader) {
		return F.Promise.pure(ErrorController.notfound());
	}

	@Override
	public F.Promise<Result> onBadRequest(Http.RequestHeader requestHeader, String s) {
		return F.Promise.pure(ErrorController.notfound());
	}

	@Override
	public F.Promise<Result> onError(Http.RequestHeader requestHeader, Throwable e) {
		Logger.error(TAG, e);

		if (requestHeader.method().equalsIgnoreCase("POST") && requestHeader.path().startsWith("/api")) {
			return F.Promise.pure(ErrorController.api_error(e));
		}
		return F.Promise.pure(ErrorController.error(e));
	}
}