package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.response.Response;
import src.controllers.session.SessionHelper;
import src.models.User;
import src.user.Permission;
import src.util.Logger;
import src.util.dbimport.INCI;
import views.html.admin_home;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AdminController extends Controller {
	public static Result home() {
		User user = SessionHelper.getUser(session());

		if (user == null || !user.hasPermission(Permission.ADMIN_ALL)) {
			return ErrorController.notfound();
		}

		return ok(admin_home.render(null));
	}

	public static Result api_auto_import() {
		User user = SessionHelper.getUser(session());

		if (user == null || !user.hasPermission(Permission.ADMIN_IMPORT)) {
			return API.writeResponse(new Response(Response.UNAUTHORIZED).setError("You are not allowed to do that!"));
		}

		try {
			byte[] data = Files.readAllBytes(Paths.get("php/data/specialchem-ingredients.json.txt"));
			String inci = new String(data);
			INCI.importDB(inci);
		}
		catch (IOException e) {
			Logger.error("AdminController", e);
		}

		System.gc();

		return API.writeResponse(new Response());
	}
}
