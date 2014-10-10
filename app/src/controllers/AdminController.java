package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.response.Response;
import src.api.response.ResponseMessage;
import src.controllers.session.SessionHelper;
import src.models.User;
import src.user.Permission;
import src.util.Logger;
import src.util.dbimport.ImportIngredients;
import src.views.RenderState;
import views.html.admin_home;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AdminController extends Controller {
	private static final String TAG = "AdminController";

	public static Result home() {
		RenderState state = new RenderState(session());

		if (state.getUser() == null || !state.getUser().hasPermission(Permission.ADMIN_ALL)) {
			return ErrorController.notfound();
		}

		return ok(admin_home.render(state));
	}

	public static Result api_auto_import() {
		User user = SessionHelper.getUser(session());

		if (user == null || !user.hasPermission(Permission.ADMIN_IMPORT)) {
			return API.writeResponse(new Response(Response.UNAUTHORIZED)
					.addMessage(ResponseMessage.error("You are not allowed to do that!")));
		}

		Logger.info(TAG, "DB import started");

		try {
			byte[] data = Files.readAllBytes(Paths.get("php/data/specialchem-ingredients.json.txt"));
			String inci = new String(data);
			ImportIngredients.importDB(inci);
		}
		catch (IOException e) {
			Logger.error(TAG, e);
		}

		Logger.info(TAG, "DB import finished");

		System.gc();

		return API.writeResponse(new Response().addMessage(ResponseMessage.info("Import success")));
	}
}
