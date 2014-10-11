package src.controllers.api.request;

import src.controllers.api.response.Response;

public class UnauthorizedException extends BadRequestException {
	public UnauthorizedException(String error) {
		super(Response.UNAUTHORIZED, error);
	}
}
