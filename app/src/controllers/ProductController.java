package src.controllers;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.api.Api;
import src.api.ProductApi;
import src.api.request.BadRequestException;
import src.api.response.ErrorResponse;
import src.api.response.Response;
import src.models.product.Product;

public class ProductController extends Controller {
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_info() {
		try {
			ProductApi.RequestProductInfo request =
					Api.read(ctx(), ProductApi.RequestProductInfo.class);

			Product result = Product.byId(request.product_id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Product not found");
			}

			Response response = new ProductApi.ResponseProductInfo(
					result.getName(),
					result.getBrand(),
					result.getDescription()
			);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
