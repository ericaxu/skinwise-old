package src.api.response;

import src.api.request.BadRequestException;

public class ErrorResponse extends Response {
	public ErrorResponse(BadRequestException e) {
		this(e.getCode(), e.getError());
	}

	public ErrorResponse(String code, String error) {
		setCode(code);
		if (error != null && !error.isEmpty()) {
			addMessage(ResponseMessage.error(error));
		}
	}
}
