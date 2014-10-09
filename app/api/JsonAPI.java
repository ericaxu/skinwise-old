package api;

import api.request.BadRequestException;
import api.request.Request;
import api.response.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import util.Logger;

import java.io.IOException;

public class JsonAPI {
	private static final String TAG = "JsonApi";
	private static volatile ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static ObjectMapper mapper() {
		return objectMapper;
	}

	public static String serialize(Object object) {
		try {
			return mapper().writeValueAsString(object);
		}
		catch (JsonProcessingException e) {
			Logger.fatal(TAG, "JSON Serialization error", e);
		}
		return "";
	}

	public static <T> T deserialize(String input, Class<T> clazz)
			throws IOException {
		//If the request is null or empty, pass in an empty object to satisfy jackson
		if (input == null || input.isEmpty()) {
			input = "{}";
		}
		return mapper().readValue(input, clazz);
	}

	public static String writeRequest(Request request) {
		return serialize(request);
	}

	public static String writeResponse(Response response) {
		return serialize(response);
	}

	public static <T extends Request> T readRequest(String input, Class<T> clazz)
			throws BadRequestException {
		try {
			return deserialize(input, clazz);
		}
		catch (IOException e) {
			throw new BadRequestException(Response.BAD_JSON);
		}
	}

	public static <T extends Response> T readResponse(String input, Class<T> clazz)
			throws IOException {
		return deserialize(input, clazz);
	}
}
