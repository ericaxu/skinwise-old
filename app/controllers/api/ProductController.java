package controllers.api;

import api.API;
import api.ProductAPI;
import api.response.Response;
import models.Product;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class ProductController extends Controller {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result info(long product_id) {
		Product product = Product.byId(product_id);
		Response response = new ProductAPI.ResponseProductInfo(product.getName(),
														       product.getBrand(),
				                                               product.getDescription());

		return API.writeResponse(ctx(), response);
	}
}
