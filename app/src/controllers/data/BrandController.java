package src.controllers.data;

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
}
