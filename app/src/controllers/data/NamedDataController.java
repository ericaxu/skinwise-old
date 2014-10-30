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
		try {
			Api.RequestGetById request = Api.read(context, Api.RequestGetById.class);

			NamedModel result = index.get(request.id);
			Api.checkNotNull(result, title, request.id);

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = 1;

			response.results.add(new Api.ResponseNamedModelObject(
					result.getId(),
					result.getName(),
					result.getDescription()
			));

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_named_model_all(MemCache.NamedIndex<? extends NamedModel> index) {
		Api.ResponseResultList response = new Api.ResponseResultList();

		for (NamedModel object : index.all()) {
			response.results.add(new Api.ResponseNamedModelObject(
					object.getId(),
					object.getName(),
					object.getDescription()
			));
		}

		return Api.write(response);
	}
}
