package src.controllers.api;

import src.api.JsonAPI;
import src.api.request.BadRequestException;
import src.api.request.NotEmpty;
import src.api.request.NotNull;
import src.api.request.Request;
import src.api.response.Response;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Results;

import java.lang.reflect.Field;

public class API {
	public static <T extends Request> T readRequest(Http.Context context,
	                                                Class<? extends T> clazz)
			throws BadRequestException {
		T request = JsonAPI.readRequest(context.request().body().asText(), clazz);

		//Check fields satisfy request specifications
		validateFields(request);
		return request;
	}

	private static void validateFields(Request request) throws BadRequestException {
		Field[] fields = request.getClass().getFields();

		for (Field field : fields) {

			Object data = null;
			try {
				data = field.get(request);
			}
			catch (IllegalAccessException e) {
				//TODO: Log this
				e.printStackTrace();
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

	public static Results.Status writeResponse(Response response) {
		String data = JsonAPI.writeResponse(response);
		return Controller.ok(data);
	}
}
