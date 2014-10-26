package src.controllers.data;

import org.apache.commons.lang3.text.WordUtils;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.MemCache;
import src.models.Page;
import src.models.data.*;
import views.html.brand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrandController extends Controller {
	public static class ResponseBrand extends Response {
		public long id;
		public String name;

		public ResponseBrand(long id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	public static Result brand(long brand_id) {
		ResponseState state = new ResponseState(session());

		Brand result = App.cache().brands.get(brand_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(brand.render(state, result));
	}
}
