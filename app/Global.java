import controllers.ErrorController;
import models.Permission;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import util.dbimport.INCI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Global extends GlobalSettings {
	@Override
	public void onStart(Application app) {
		String admin_email = "admin@skinwise.com";
		String admin_pass = "test@123?!";
		User admin = User.byEmail(admin_email);
		if (admin == null) {
			admin = new User(admin_email, admin_pass, "Admin");
			admin.addPermission(Permission.ADMIN_ALL);
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