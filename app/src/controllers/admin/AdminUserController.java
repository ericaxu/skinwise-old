package src.controllers.admin;

import org.hibernate.validator.constraints.NotEmpty;
import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.api.response.ResponseMessage;
import src.controllers.util.ResponseState;
import src.models.Permissible;
import src.models.user.User;
import src.models.user.Usergroup;

import java.util.Set;

public class AdminUserController extends Controller {
	private static final String TAG = "AdminUserController";

	public static class RequestGetUserByEmail extends Request {
		@NotEmpty
		public String email;
	}

	public static class RequestGetGroupByName extends Request {
		@NotEmpty
		public String name;
	}

	public static class ResponseUser extends Response {
		public long id;
		public String email;
		public String name;
		public String group;
		public Set<String> permissions;

		public ResponseUser(long id, String email, String name,
		                    String group, Set<String> permissions) {
			this.id = id;
			this.email = email;
			this.name = name;
			this.group = group;
			this.permissions = permissions;
		}
	}

	public static class ResponseGroup extends Response {
		public long id;
		public String name;
		public Set<String> permissions;

		public ResponseGroup(long id, String name, Set<String> permissions) {
			this.id = id;
			this.name = name;
			this.permissions = permissions;
		}
	}

	public static class RequestEditUser extends Request {
		public long id;
		@NotEmpty
		public String email;
		@NotEmpty
		public String name;
		@NotNull
		public String group;
		public Set<String> permissions;
	}

	public static class RequestSetUserPassword extends Request {
		public long id;
		@NotEmpty
		public String password;
	}

	public static class RequestEditGroup extends Request {
		public long id;
		@NotEmpty
		public String name;
		public Set<String> permissions;
	}

	public static Result api_user_by_id() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			User result = User.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			return Api.write(getUserResponse(result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_user_by_email() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			RequestGetUserByEmail request = Api.read(ctx(), RequestGetUserByEmail.class);

			User result = User.byEmail(request.email);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Email " + request.email + " not found");
			}

			return Api.write(getUserResponse(result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_user_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			RequestEditUser request =
					Api.read(ctx(), RequestEditUser.class);

			User result = User.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			Usergroup group = Usergroup.byName(request.group);

			result.setName(request.name);
			result.setEmail(request.email);
			result.setGroup(group);
			result.setPermissions_set(request.permissions);

			result.save();

			state.setResponse(getUserResponse(result));
			state.getResponse().addMessage(ResponseMessage.info("Successfully updated user " + request.id));

			return Api.write(state.getResponse());
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_user_set_password() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			RequestSetUserPassword request = Api.read(ctx(), RequestSetUserPassword.class);

			User result = User.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			result.setPassword(request.password);

			result.save();

			state.setResponse(getUserResponse(result));
			state.getResponse().addMessage(ResponseMessage.info("Successfully changed password for user " + request.id));

			return Api.write(state.getResponse());
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_user_delete() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			User result = User.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			result.delete();

			return Api.write(new InfoResponse("Successfully deleted user " + request.id));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_group_by_id() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Usergroup result = Usergroup.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			return Api.write(getGroupResponse(result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_group_by_name() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			RequestGetGroupByName request = Api.read(ctx(), RequestGetGroupByName.class);

			Usergroup result = Usergroup.byName(request.name);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Group " + request.name + " not found");
			}

			return Api.write(getGroupResponse(result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_group_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			RequestEditGroup request = Api.read(ctx(), RequestEditGroup.class);

			Usergroup result = Usergroup.byId(request.id);

			if (request.id == -1) {
				result = new Usergroup();
			}

			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			result.setName(request.name);
			result.setPermissions_set(request.permissions);

			result.save();

			state.setResponse(getGroupResponse(result));
			state.getResponse().addMessage(ResponseMessage.info("Successfully updated group " + request.id));

			return Api.write(state.getResponse());
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_group_delete() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.USER);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Usergroup result = Usergroup.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			result.delete();

			return Api.write(new InfoResponse("Successfully deleted group " + request.id));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	private static Response getUserResponse(User user) {
		return new ResponseUser(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getGroupName(),
				user.getPermissions_set()
		);
	}

	private static Response getGroupResponse(Usergroup group) {
		return new ResponseGroup(
				group.getId(),
				group.getName(),
				group.getPermissions_set()
		);
	}
}
