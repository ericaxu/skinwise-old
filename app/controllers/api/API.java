package api;

import api.request.Request;
import api.request.*;
import api.response.Response;
import api.response.ResponseStatus;
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

		//TODO: Try to load and verify session if secure request
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
					throw new BadRequestException(ResponseStatus.INVALID);
				}
			}

			if (field.isAnnotationPresent(NotEmpty.class)) {
				if (data instanceof String && ((String) data).isEmpty()) {
					throw new BadRequestException(ResponseStatus.INVALID);
				}
			}
		}
	}

	public static Response response(ResponseStatus status) {
		return new Response(status);
	}

	public static Response response(BadRequestException e) {
		return new Response(e);
	}

	public static Response response(Response response) {
		return response;
	}

	public static Results.Status writeResponse(Http.Context context, Response response) {
		response.getStatus().updateServerTime();
		String data = null;
		data = JsonAPI.writeResponse(response);
		return Controller.ok(data);
	}
}
