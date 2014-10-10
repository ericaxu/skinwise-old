package src.api.response;

import java.util.ArrayList;
import java.util.List;

public class Response {
	public transient static final String OK = "Ok";
	public transient static final String BAD_JSON = "BadJson";
	public transient static final String INVALID = "Invalid";
	public transient static final String INTERNAL_ERROR = "InternalError";
	public transient static final String UNAUTHORIZED = "Unauthorized";
	public transient static final String NOT_FOUND = "NotFound";

	private String code = "";
	private List<ResponseMessage> messages = new ArrayList<>();

	public Response() {
		this(OK);
	}

	public Response(String code) {
		setCode(code);
	}

	public Response setCode(String code) {
		if (code == null) {
			code = "";
		}

		this.code = code;
		return this;
	}

	public String getCode() {
		return code;
	}

	public List<ResponseMessage> getMessages() {
		return messages;
	}

	public Response addMessage(ResponseMessage message) {
		this.messages.add(message);
		return this;
	}

	@Override
	public String toString() {
		return code;
	}
}