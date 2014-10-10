package src.api;

import src.api.request.NotEmpty;
import src.api.request.Request;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAPI {
	public transient static final String EMAIL_TAKEN = "EmailTaken";

	public static class RequestLogin extends Request {
		@NotEmpty
		public String email;
		@NotEmpty
		public String password;

		public RequestLogin(@JsonProperty("email") String email,
		                    @JsonProperty("password") String password) {
			this.email = email;
			this.password = password;
		}
	}

	public static class RequestSignup extends Request {
		@NotEmpty
		public String name;
		@NotEmpty
		public String email;
		@NotEmpty
		public String password;

		public RequestSignup(@JsonProperty("name") String name,
		                     @JsonProperty("email") String email,
		                     @JsonProperty("password") String password) {
			this.name = name;
			this.email = email;
			this.password = password;
		}
	}
}
