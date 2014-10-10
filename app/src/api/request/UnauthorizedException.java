package src.api.request;

import src.api.response.Response;

public class UnauthorizedException extends BadRequestException {
	public UnauthorizedException(String error) {
		super(Response.UNAUTHORIZED, error);
	}
}
