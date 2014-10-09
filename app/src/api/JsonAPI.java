package src.api;

import src.api.request.BadRequestException;
import src.api.request.Request;
import src.api.response.Response;
import src.util.Json;

import java.io.IOException;

public class JsonAPI {
	private static final String TAG = "JsonApi";

	public static String writeRequest(Request request) {
		return Json.serialize(request);
	}

	public static String writeResponse(Response response) {
		return Json.serialize(response);
	}

	public static <T extends Request> T readRequest(String input, Class<T> clazz)
			throws BadRequestException {
		try {
			return Json.deserialize(input, clazz);
		}
		catch (IOException e) {
			throw new BadRequestException(Response.BAD_JSON);
		}
	}

	public static <T extends Response> T readResponse(String input, Class<T> clazz)
			throws IOException {
		return Json.deserialize(input, clazz);
	}
}
