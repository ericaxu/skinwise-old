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
import views.html.admin;
import views.html.content_admin;

public class AdminController extends Controller {
	private static final String TAG = "AdminController";

	public static Result home() {
		ResponseState state = new ResponseState(session());

		if (!state.userHasPermission(Permissible.ADMIN.VIEW)) {
			return ErrorController.notfound();
		}

		return ok(admin.render(state));
	}

	public static Result content() {
		ResponseState state = new ResponseState(session());

		if (!state.userHasPermission(Permissible.ADMIN.VIEW)) {
			return ErrorController.notfound();
		}

		return ok(content_admin.render(state));
	}

	public static Result api_import_db() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.IMPORT);

			Logger.info(TAG, "DB import started");

			try {
				Import.importDB("php/data/data.json.txt");
			}
			catch (Exception e) {
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

	public static Result api_export_db() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.EXPORT);

			Logger.info(TAG, "DB export started");

			try {
				Export.exportDB("php/data/export.json.txt");
			}
			catch (Exception e) {
				Logger.error(TAG, e);
			}

			Logger.info(TAG, "DB export finished");

			System.gc();

			return Api.write(new InfoResponse("Export success"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
