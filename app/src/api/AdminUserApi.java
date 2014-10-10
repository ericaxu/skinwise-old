package src.api;

import org.hibernate.validator.constraints.NotEmpty;
import src.api.request.Request;
import src.api.response.Response;

public class AdminUserAPI {
	public static class RequestGetUserById extends Request {
		public long id;
	}

	public static class RequestGetUserByEmail extends Request {
		@NotEmpty
		public String email;
	}

	public static class ResponseUser extends Response {
		public long id;
		public String email;
		public String name;

		public ResponseUser(long id, String email, String name) {
			this.id = id;
			this.email = email;
			this.name = name;
		}
	}
}
