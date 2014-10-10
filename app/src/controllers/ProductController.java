package src.controllers;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.api.API;
import src.api.ProductAPI;
import src.api.response.Response;
import src.api.response.ResponseMessage;
import src.models.Product;

public class ProductController extends Controller {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_info(long product_id) {
		Product product = Product.byId(product_id);
		if (product == null) {
			return API.write(new Response(Response.NOT_FOUND)
					.addMessage(ResponseMessage.error("Product not found")));
		}

		Response response = new ProductAPI.ResponseProductInfo(
				product.getName(),
				product.getBrand(),
				product.getDescription()
		);

		return API.write(response);
	}
}
