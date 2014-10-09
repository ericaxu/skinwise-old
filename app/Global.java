import src.controllers.ErrorController;
import src.models.Permission;
import src.models.User;
import src.models.Usergroup;
import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import src.util.dbimport.INCI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Global extends GlobalSettings {
	public static final String ADMIN_USER_GROUP = "Administrators";

	@Override
	public void onStart(Application app) {
		Usergroup admin_group = Usergroup.byName(ADMIN_USER_GROUP);
		if (admin_group == null) {
			admin_group = new Usergroup(ADMIN_USER_GROUP);
			admin_group.addPermission(Permission.ADMIN_ALL);
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

		try {
			byte[] data = Files.readAllBytes(Paths.get("data/inci.json.txt"));
			String inci = new String(data);
			INCI.importDB(inci);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStop(Application app) {
	}

	@Override
	public F.Promise<Result> onHandlerNotFound(Http.RequestHeader requestHeader) {
		return F.Promise.pure(ErrorController.notfound());
	}
}