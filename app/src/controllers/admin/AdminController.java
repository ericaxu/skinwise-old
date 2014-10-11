package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.util.ResponseState;
import src.models.Permissible;
import src.util.Logger;
import src.util.dbimport.ImportIngredients;
import views.html.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AdminController extends Controller {
	private static final String TAG = "AdminController";

	public static Result home() {
		ResponseState state = new ResponseState(session());

		if (!state.userHasPermission(Permissible.ADMIN.VIEW)) {
			return ErrorController.notfound();
		}

		return ok(admin.render(state));
	}

	public static Result api_auto_import() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.IMPORT);

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

			return Api.write(new InfoResponse("Import success"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
