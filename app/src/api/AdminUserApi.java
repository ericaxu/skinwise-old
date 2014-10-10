package src.api;

import org.hibernate.validator.constraints.NotEmpty;
import src.api.request.Request;
import src.api.response.Response;

import java.util.Set;

public class AdminUserApi {
	public static class RequestGetById extends Request {
		public long id;
	}

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
}
