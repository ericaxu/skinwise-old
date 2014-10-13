package src.controllers.data;

import org.apache.commons.lang3.text.WordUtils;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.data.Ingredient;
import src.models.data.IngredientName;
import src.models.data.Product;
import views.html.product;

import java.util.ArrayList;
import java.util.List;

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

	public static class IngredientInfo {
		public String name;
		public String description;
		public List<String> functions;

		public IngredientInfo(String name, String description, List<String> functions) {
			this.name = name;
			this.description = description;
			this.functions = functions;
		}
	}

	public static class ResponseProductIngredientInfo extends Response {
		public List<IngredientInfo> ingredient_info;

		public ResponseProductIngredientInfo(List<Ingredient> ingredients) {
			this.ingredient_info = new ArrayList<>();

			for (Ingredient ingredient : ingredients) {
				this.ingredient_info.add(new IngredientInfo(
						WordUtils.capitalizeFully(ingredient.getName()),
						ingredient.getDescription(),
						ingredient.getFunctionsString()
				));
			}
		}
	}

	public static Result info(long product_id) {
		try {
			ResponseState state = new ResponseState(session());
			Product result = Product.byId(product_id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Ingredient not found");
			}

			return ok(product.render(state, result));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_ingredient_info() {
		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Product result = Product.byId(request.id);

			List<Ingredient> ingredients = new ArrayList<>();

			for (IngredientName ingredient_name : result.getKey_ingredients()) {
				if (ingredient_name.getIngredient() != null) {
					ingredients.add(ingredient_name.getIngredient());
				}
			}

			for (IngredientName ingredient_name : result.getIngredients()) {
				if (ingredient_name.getIngredient() != null) {
					ingredients.add(ingredient_name.getIngredient());
				}
			}

			Response response = new ResponseProductIngredientInfo(ingredients);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
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
