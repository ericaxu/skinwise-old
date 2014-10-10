package src.views;

import play.mvc.Http;
import src.api.response.Response;
import src.controllers.session.SessionHelper;
import src.models.User;

public class ResponseState {
	private User user;
	private Response response;

	public ResponseState(Http.Session session) {
		this.user = SessionHelper.getUser(session);
		this.response = new Response();
	}

	public User getUser() {
		return user;
	}

	public Response getResponse() {
		return response;
	}

	public boolean userHasPermission(String permission) {
		if (user == null) {
			return false;
		}

		return user.hasPermission(permission);
	}
}
