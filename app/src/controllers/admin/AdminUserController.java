package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import src.api.AdminUserApi;
import src.api.Api;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.Permissible;
import src.models.user.User;
import src.models.user.Usergroup;

public class AdminUserController extends Controller {
	private static final String TAG = "AdminUserController";

	public static Result api_get_user_by_id() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			AdminUserApi.RequestGetById request =
					Api.read(ctx(), AdminUserApi.RequestGetById.class);

			User result = User.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "User id " + request.id + " not found");
			}

			return Api.write(getUserResponse(result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_get_user_by_email() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			AdminUserApi.RequestGetUserByEmail request =
					Api.read(ctx(), AdminUserApi.RequestGetUserByEmail.class);

			User result = User.byEmail(request.email);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "User email " + request.email + " not found");
			}

			return Api.write(getUserResponse(result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_get_group_by_id() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			AdminUserApi.RequestGetById request =
					Api.read(ctx(), AdminUserApi.RequestGetById.class);

			Usergroup result = Usergroup.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Group id " + request.id + " not found");
			}

			return Api.write(getGroupResponse(result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_get_group_by_name() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			AdminUserApi.RequestGetGroupByName request =
					Api.read(ctx(), AdminUserApi.RequestGetGroupByName.class);

			Usergroup result = Usergroup.byName(request.name);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Group name " + request.name + " not found");
			}

			return Api.write(getGroupResponse(result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_edit_user() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			AdminUserApi.RequestEditUser request =
					Api.read(ctx(), AdminUserApi.RequestEditUser.class);

			User result = User.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "User id " + request.id + " not found");
			}

			Usergroup group = Usergroup.byName(request.group);

			result.setName(request.name);
			result.setEmail(request.email);
			result.setGroup(group);
			result.setPermissions_set(request.permissions);

			result.save();

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_edit_group() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			AdminUserApi.RequestEditGroup request =
					Api.read(ctx(), AdminUserApi.RequestEditGroup.class);

			Usergroup result = Usergroup.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Group id " + request.id + " not found");
			}

			result.setName(request.name);
			result.setPermissions_set(request.permissions);

			result.save();

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_delete_user() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			AdminUserApi.RequestGetById request =
					Api.read(ctx(), AdminUserApi.RequestGetById.class);

			User result = User.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "User id " + request.id + " not found");
			}

			result.delete();

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_delete_group() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			AdminUserApi.RequestGetById request =
					Api.read(ctx(), AdminUserApi.RequestGetById.class);

			Usergroup result = Usergroup.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Group id " + request.id + " not found");
			}

			result.delete();

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	private static Response getUserResponse(User user) {
		return new AdminUserApi.ResponseUser(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getGroupName(),
				user.getPermissions_set()
		);
	}

	private static Response getGroupResponse(Usergroup group) {
		return new AdminUserApi.ResponseGroup(
				group.getId(),
				group.getName(),
				group.getPermissions_set()
		);
	}
}
