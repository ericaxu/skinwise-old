package src.util;

import com.fasterxml.jackson.core.JsonGenerator;
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
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.getFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
	}

	public static ObjectMapper mapper() {
		return objectMapper;
	}

	public static String serialize(Object object) {
		try {
			return mapper().writeValueAsString(object);
		}
		catch (JsonProcessingException e) {
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