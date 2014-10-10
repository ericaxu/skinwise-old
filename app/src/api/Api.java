package src.api;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Results;
import src.api.request.BadRequestException;
import src.api.request.NotEmpty;
import src.api.request.NotNull;
import src.api.request.Request;
import src.api.response.Response;
import src.util.Json;
import src.util.Logger;

import java.io.IOException;
import java.lang.reflect.Field;

public class Api {
	private static final String TAG = "API";

	public static <T extends Request> T read(Http.Context context, Class<? extends T> clazz)
			throws BadRequestException {
		try {
			T request = Json.deserialize(context.request().body().asText(), clazz);

			//Check fields satisfy request specifications
			validateFields(request);

			return request;
		}
		catch (IOException e) {
			throw new BadRequestException(Response.BAD_JSON);
		}
	}

	private static void validateFields(Request request) throws BadRequestException {
		Field[] fields = request.getClass().getFields();

		for (Field field : fields) {

			Object data = null;
			try {
				data = field.get(request);
			}
			catch (IllegalAccessException e) {
				Logger.fatal(TAG, "Field not accessible!", e);
			}

			if (field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotEmpty.class)) {
				if (data == null) {
					throw new BadRequestException(Response.INVALID);
				}
			}

			if (field.isAnnotationPresent(NotEmpty.class)) {
				if (data instanceof String && ((String) data).isEmpty()) {
					throw new BadRequestException(Response.INVALID);
				}
			}
		}
	}

	public static Results.Status write() {
		return write(new Response());
	}

	public static Results.Status write(Response response) {
		String data = Json.serialize(response);
		return Controller.ok(data);
	}
}
