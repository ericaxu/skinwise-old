package api.response;

import api.request.BadRequestException;

public class Response {
	private ResponseStatus status;

	public Response() {
		this(ResponseStatus.OK.clone());
	}

	public Response(BadRequestException exception) {
		this(exception.getState());
	}

	public Response(ResponseStatus status) {
		this.status = status;
		if (status.equals(ResponseStatus.INTERNAL_ERROR)) {
			new Exception().printStackTrace();
		}
	}

	public ResponseStatus getStatus() {
		return status;
	}
}