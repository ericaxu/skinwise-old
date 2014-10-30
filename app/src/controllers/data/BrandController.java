package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.util.ResponseState;
import src.models.data.Brand;
import views.html.brand;

public class BrandController extends Controller {
	public static Result brand(long brand_id) {
		ResponseState state = new ResponseState(session());

		Brand result = App.cache().brands.get(brand_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(brand.render(state, result));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_brand_byid() {
		return NamedDataController.api_named_model_byid(ctx(), App.cache().brands, "Brand");
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_brand_all() {
		return NamedDataController.api_named_model_all(App.cache().brands);
	}
}
