package src.controllers.session;

import src.api.response.Response;
import src.models.User;
import play.mvc.Http;

public class SessionHelper {
	private static final String USER_ID = "user_id";

	public static User getUser(Http.Session session) {
		String user_id = session.get(USER_ID);
		if (user_id == null) {
			return null;
		}
		long id = -1;
		try {
			id = Long.parseLong(user_id);
		}
		catch (NumberFormatException e) {
			return null;
		}

		return User.byId(id);
	}

	public static void setUser(Http.Session session, User user) {
		if (user == null) {
			session.remove(USER_ID);
		}
		else {
			String user_id = Long.toString(user.getId());
			session.put(USER_ID, user_id);
		}
	}
}
