package src.api.response;

import src.api.request.BadRequestException;

public class Response {
	public transient static final String OK = "Ok";
	public transient static final String BAD_JSON = "BadJson";
	public transient static final String INVALID = "Invalid";
	public transient static final String INTERNAL_ERROR = "InternalError";
	public transient static final String UNAUTHORIZED = "Unauthorized";
	public transient static final String NOT_FOUND = "NotFound";

	private String code = "";
	private String info = "";
	private String error = "";

	public Response() {
		this(OK);
	}

	public Response(BadRequestException exception) {
		this(exception.getCode());
		this.setError(exception.getError());
	}

	public Response(String code) {
		if (code == null) {
			code = "";
		}

		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getInfo() {
		return info;
	}

	public String getError() {
		return error;
	}

	public Response setInfo(String info) {
		if (info == null) {
			info = "";
		}
		this.info = info;
		return this;
	}

	public Response setError(String error) {
		if (error == null) {
			error = "";
		}
		this.error = error;
		return this;
	}

	@Override
	public String toString() {
		return code + " Info: " + info + " Error: " + error;
	}
}