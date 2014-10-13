package src.controllers.api.response;

public class ResponseMessage {
	private static final String TYPE_INFO = "ingredient";
	private static final String TYPE_ERROR = "error";
	private static final int DEFAULT_TIMEOUT = 5000;

	public String type;
	public String message;
	public int timeout;

	private ResponseMessage(String type, String message, int timeout) {
		this.type = type;
		this.message = message;
		this.timeout = timeout;
	}

	public static ResponseMessage error(String message) {
		return error(message, DEFAULT_TIMEOUT);
	}

	public static ResponseMessage error(String message, int timeout) {
		return new ResponseMessage(TYPE_ERROR, message, timeout);
	}

	public static ResponseMessage info(String message) {
		return info(message, DEFAULT_TIMEOUT);
	}

	public static ResponseMessage info(String message, int timeout) {
		return new ResponseMessage(TYPE_INFO, message, timeout);
	}
}
