package src.controllers.api;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Results;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotEmpty;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.Response;
import src.util.Json;
import src.util.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Api {
	private static final String TAG = "API";

	public static class RequestObjectUpdate extends Request {
		public long id;
		@NotEmpty
		public String name;
		@NotNull
		public String description;
	}

	public static class RequestGetAllByPage extends Request {
		public int page;
	}

	public static class RequestGetById extends Request {
		public long id;
	}

	public static class RequestGetByIdAndPage extends RequestGetById {
		public int page;
	}

	public static class ResponseNamedModelObject {
		public long id;
		public String name;
		public String description;

		public ResponseNamedModelObject(long id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}
	}

	public static class ResponseResultList extends Response {
		public List<Object> results = new ArrayList<>();
		public int count;
	}

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

	public static void checkNotNull(Object object, String title, Object details) throws BadRequestException {
		if (object == null) {
			throw new BadRequestException(Response.NOT_FOUND, title + " " + details + "not found");
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
					throw new BadRequestException(Response.INVALID, "Field " + field.getName() + " cannot be null!");
				}
			}

			if (field.isAnnotationPresent(NotEmpty.class)) {
				if (data instanceof String && ((String) data).isEmpty()) {
					throw new BadRequestException(Response.INVALID, "Field " + field.getName() + " cannot be empty!");
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
