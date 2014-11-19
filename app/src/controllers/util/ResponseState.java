package src.controllers.util;

import play.mvc.Http;
import src.controllers.admin.Import;
import src.controllers.api.request.UnauthorizedException;
import src.controllers.api.response.Response;
import src.controllers.api.response.ResponseMessage;
import src.models.user.User;

public class ResponseState {
	private User user;
	private Response response;

	public ResponseState(Http.Session session) {
		this.user = SessionHelper.getUser(session);
		this.response = new Response();
		if (importing()) {
			this.response.addMessage(ResponseMessage.info("We're fixing some stuff real quick. " +
					"Some information might not show up properly just yet.", -1));
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {

		this.user = user;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public boolean importing() {
		return Import.importing.get();
	}

	public boolean userHasPermission(String... permissions) {
		if (user == null) {
			return false;
		}

		for (String permission : permissions) {
			if (user.hasPermission(permission)) {
				return true;
			}
		}
		return false;
	}

	public void requirePermission(String... permissions) throws UnauthorizedException {
		if (!userHasPermission(permissions)) {
			throw new UnauthorizedException("You are not allowed to do that!");
		}
	}
}
