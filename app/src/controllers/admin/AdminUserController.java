package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.AdminUserAPI;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.Response;
import src.models.User;
import src.user.Permission;
import src.views.ResponseState;

public class AdminUserController extends Controller {
	private static final String TAG = "AdminUserController";

	public static Result api_get_user_by_id() {
		ResponseState state = new ResponseState(session());

		try {
			if (!state.userHasPermission(Permission.ADMIN.USER)) {
				throw new BadRequestException(Response.UNAUTHORIZED, "You are not allowed to do that!");
			}

			AdminUserAPI.RequestGetUserById request =
					API.read(ctx(), AdminUserAPI.RequestGetUserById.class);

			User user = User.byId(request.id);
			if (user == null) {
				throw new BadRequestException(Response.NOT_FOUND, "User id " + request.id + " not found");
			}

			return API.write(getUserResponse(user));
		}
		catch (BadRequestException e) {
			return API.write(new ErrorResponse(e));
		}
	}

	public static Result api_get_user_by_email() {
		ResponseState state = new ResponseState(session());

		try {
			if (!state.userHasPermission(Permission.ADMIN.USER)) {
				throw new BadRequestException(Response.UNAUTHORIZED, "You are not allowed to do that!");
			}

			AdminUserAPI.RequestGetUserByEmail request =
					API.read(ctx(), AdminUserAPI.RequestGetUserByEmail.class);

			User user = User.byEmail(request.email);
			if (user == null) {
				throw new BadRequestException(Response.NOT_FOUND, "User email " + request.email + " not found");
			}

			return API.write(getUserResponse(user));
		}
		catch (BadRequestException e) {
			return API.write(new ErrorResponse(e));
		}
	}

	private static Response getUserResponse(User user) {
		return new AdminUserAPI.ResponseUser(
				user.getId(),
				user.getEmail(),
				user.getName());
	}

}
