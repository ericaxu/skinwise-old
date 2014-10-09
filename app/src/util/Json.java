package src.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public class Json {
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
			//TODO
			Logger.fatal(TAG, "Json serialization error", e);
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
}