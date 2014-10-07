package api.response;

public class ResponseStatus {
	public transient static final ResponseStatus OK = new ResponseStatus("Ok");
	public transient static final ResponseStatus BAD_JSON = new ResponseStatus("BadJson");
	public transient static final ResponseStatus INVALID = new ResponseStatus("Invalid");
	public transient static final ResponseStatus INTERNAL_ERROR = new ResponseStatus("InternalError");
	public transient static final ResponseStatus UNAUTHORIZED = new ResponseStatus("Unauthorized");
	public transient static final ResponseStatus SESSION_EXPIRED = new ResponseStatus("SessionExpired");

	private String code;
	private String message;
	private long server_time;

	public ResponseStatus(String code, String message, long server_time) {
		if (code == null) {
			code = "";
		}
		if (message == null) {
			message = "";
		}
		if (server_time == 0) {
			server_time = System.currentTimeMillis();
		}

		this.code = code;
		this.message = message;
		this.server_time = server_time;
	}

	public ResponseStatus(String code) {
		this(code, "", 0);
	}

	public ResponseStatus() {
		this("", "", 0);
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public long getServer_time() {
		return server_time;
	}

	public void updateServerTime() {
		this.server_time = System.currentTimeMillis();
	}

	public ResponseStatus clone() {
		return new ResponseStatus(code, message, server_time);
	}

	@Override
	public String toString() {
		return code + " " + message;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ResponseStatus) {
			ResponseStatus other = (ResponseStatus) obj;
			return code.equals(other.code) && message.equals(other.message);
		}
		return false;
	}
}