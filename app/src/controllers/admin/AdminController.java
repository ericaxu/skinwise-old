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
import src.util.Util;
import src.util.dbimport.Import;
import src.util.dbimport.ImportIngredients;
import src.util.dbimport.ImportProducts;
import views.html.admin;

import java.io.IOException;

public class AdminController extends Controller {
	private static final String TAG = "AdminController";

	public static Result home() {
		ResponseState state = new ResponseState(session());

		if (!state.userHasPermission(Permissible.ADMIN.VIEW)) {
			return ErrorController.notfound();
		}

		return ok(admin.render(state));
	}

	public static Result api_import_ingredients() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.IMPORT);

			Logger.info(TAG, "DB import started");

			try {
				ImportIngredients.importDB("php/data/specialchem-ingredients.json.txt");
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

	public static Result api_import_products() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.IMPORT);

			Logger.info(TAG, "DB import started");

			try {
				String input = Util.readAll("data/products.json.txt");
				Import.ImportResult result = ImportProducts.importDB(input);
				Util.writeAll("data/products.valid.json.txt", result.valid);
				Util.writeAll("data/products.invalid.json.txt", result.invalid);
				Util.writeAll("data/products.failed.json.txt", result.failedReasons.replace("\", \"", "\",\n\""));
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
