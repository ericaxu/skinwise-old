package src.api;

import src.api.request.NotEmpty;
import src.api.request.Request;

public class UserApi {
	public transient static final String EMAIL_TAKEN = "EmailTaken";

	public static class RequestLogin extends Request {
		@NotEmpty
		public String email;
		@NotEmpty
		public String password;
	}

	public static class RequestSignup extends Request {
		@NotEmpty
		public String name;
		@NotEmpty
		public String email;
		@NotEmpty
		public String password;

		public RequestSignup(String name, String email, String password) {
			this.name = name;
			this.email = email;
			this.password = password;
		}
	}
}
