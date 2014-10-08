package api;

import api.request.NotEmpty;
import api.request.Request;
import api.response.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAPI {
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
}
