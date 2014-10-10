package src.views;

import play.mvc.Http;
import src.api.response.Response;
import src.controllers.session.SessionHelper;
import src.models.User;

public class RenderState {
	private User user;
	private Response response;

	public RenderState(Http.Session session) {
		this.user = SessionHelper.getUser(session);
		this.response = new Response();
	}

	public User getUser() {
		return user;
	}

	public Response getResponse() {
		return response;
	}
}
