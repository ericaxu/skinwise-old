package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.models.data.product.Product;

public class ProductController extends Controller {
	public static class ResponseProductInfo extends Response {
		public String product_name;
		public String product_brand;
		public String description;

		public ResponseProductInfo(String product_name, String product_brand, String description) {
			this.product_name = product_name;
			this.product_brand = product_brand;
			this.description = description;
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_info() {
		try {
			Api.RequestGetById request =
					Api.read(ctx(), Api.RequestGetById.class);

			Product result = Product.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Product not found");
			}

			Response response = new ResponseProductInfo(
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
