package src.controllers;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.api.Api;
import src.api.ProductApi;
import src.api.response.Response;
import src.api.response.ResponseMessage;
import src.models.Product;

public class ProductController extends Controller {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_info(long product_id) {
		Product product = Product.byId(product_id);
		if (product == null) {
			return Api.write(new Response(Response.NOT_FOUND)
					.addMessage(ResponseMessage.error("Product not found")));
		}

		Response response = new ProductApi.ResponseProductInfo(
				product.getName(),
				product.getBrand(),
				product.getDescription()
		);

		return Api.write(response);
	}
}
