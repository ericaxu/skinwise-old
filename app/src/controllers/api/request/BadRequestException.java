package src.controllers.api.request;

public class BadRequestException extends Exception {
	private String code;
	private String error;

	public BadRequestException(String code) {
		this(code, "");
	}

	public BadRequestException(String code, String error) {
		this.code = code;
		this.error = error;
	}

	public String getCode() {
		return code;
	}

	public String getError() {
		return error;
	}

	@Override
	public String toString() {
		return super.toString() + " (BadRequestException: " + code + " Error: " + error + ")";
	}
}