package api.request;

import api.response.ResponseStatus;

public class BadRequestException extends Exception {
	private ResponseStatus state;

	public BadRequestException(ResponseStatus state) {
		this.state = state;
	}

	public ResponseStatus getState() {
		return state;
	}

	@Override
	public String toString() {
		return super.toString() + " (BadRequestException: " + state.toString() + ")";
	}
}