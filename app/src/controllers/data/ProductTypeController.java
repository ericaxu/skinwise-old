package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.util.ResponseState;
import src.models.data.ProductType;

public class ProductTypeController extends Controller {
	public static Result product_type(long product_type_id) {
		ResponseState state = new ResponseState(session());

		ProductType result = App.cache().types.get(product_type_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(/*product_type.render(state, result)*/);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_type_byid() {
		return NamedDataController.api_named_model_byid(ctx(), App.cache().types, "Product Type");
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_type_all() {
		return NamedDataController.api_named_model_all(App.cache().types);
	}
}
