package src.controllers.api;

import src.api.ProductAPI;
import src.api.response.Response;
import src.models.Product;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class ProductController extends Controller {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result info(long product_id) {
		Product product = Product.byId(product_id);
		if (product == null) {
			return API.writeResponse(new Response(Response.OK).setError("Product not found"));
		}

		Response response = new ProductAPI.ResponseProductInfo(
				product.getName(),
				product.getBrand(),
				product.getDescription()
		);

		return API.writeResponse(response);
	}
}
