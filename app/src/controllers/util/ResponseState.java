package src.controllers.util;

import play.mvc.Http;
import src.api.response.Response;
import src.models.user.User;

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

	public void setUser(User user) {

		this.user = user;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public boolean userHasPermission(String permission) {
		if (user == null) {
			return false;
		}

		return user.hasPermission(permission);
	}
}
