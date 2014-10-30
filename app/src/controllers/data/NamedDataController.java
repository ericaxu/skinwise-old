package src.controllers.data;

import play.mvc.Http;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.models.MemCache;
import src.models.util.NamedModel;

public class NamedDataController {
	public static Result api_named_model_byid(Http.Context context,
	                                          MemCache.NamedIndex<? extends NamedModel> index,
	                                          String title) {
		return api_named_model_byid(context, index, title, null);
	}

	public static <T extends NamedModel> Result api_named_model_byid(Http.Context context,
	                                                                 MemCache.NamedIndex<T> index,
	                                                                 String title,
	                                                                 Serializer<T> serializer) {
		if (serializer == null) {
			serializer = new Serializer<>();
		}

		try {
			Api.RequestGetById request = Api.read(context, Api.RequestGetById.class);

			T result = index.get(request.id);
			Api.checkNotNull(result, title, request.id);

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = 1;

			response.results.add(serializer.create(result));

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static <T extends NamedModel> Result api_named_model_all(MemCache.NamedIndex<T> index) {
		return api_named_model_all(index, null);
	}

	public static <T extends NamedModel> Result api_named_model_all(MemCache.NamedIndex<T> index,
	                                                                Serializer<T> serializer) {
		if (serializer == null) {
			serializer = new Serializer<>();
		}

		Api.ResponseResultList response = new Api.ResponseResultList();

		for (T object : index.all()) {
			response.results.add(serializer.create(object));
		}

		response.count = response.results.size();

		return Api.write(response);
	}

	public static class Serializer<T extends NamedModel> {
		public Api.ResponseNamedModelObject create(T object) {
			return new Api.ResponseNamedModelObject(
					object.getId(),
					object.getName(),
					object.getDescription()
			);
		}
	}
}
