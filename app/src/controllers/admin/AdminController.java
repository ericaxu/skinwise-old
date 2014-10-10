package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.InfoResponse;
import src.api.response.Response;
import src.controllers.ErrorController;
import src.user.Permission;
import src.util.Logger;
import src.util.dbimport.ImportIngredients;
import src.views.ResponseState;
import views.html.admin_home;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AdminController extends Controller {
	private static final String TAG = "AdminController";

	public static Result home() {
		ResponseState state = new ResponseState(session());

		if (!state.userHasPermission(Permission.ADMIN.VIEW)) {
			return ErrorController.notfound();
		}

		return ok(admin_home.render(state));
	}

	public static Result api_auto_import() {
		ResponseState state = new ResponseState(session());

		try {
			if (!state.userHasPermission(Permission.ADMIN.IMPORT)) {
				throw new BadRequestException(Response.UNAUTHORIZED, "You are not allowed to do that!");
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

			return API.write(new InfoResponse("Import success"));
		}
		catch (BadRequestException e) {
			return API.write(new ErrorResponse(e));
		}
	}
}
