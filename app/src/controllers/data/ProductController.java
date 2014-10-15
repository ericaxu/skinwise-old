package src.controllers.data;

import org.apache.commons.lang3.text.WordUtils;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotNull;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.Page;
import src.models.data.Ingredient;
import src.models.data.Product;
import src.models.data.ProductIngredient;
import views.html.product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public static class ResponseIngredientObject {
		public String name;
		public String description;
		public List<String> functions;

		public ResponseIngredientObject(String name, String description, List<String> functions) {
			this.name = name;
			this.description = description;
			this.functions = functions;
		}
	}

	public static class ResponseProductIngredientInfo extends Response {
		public List<ResponseIngredientObject> ingredient_info;

		public ResponseProductIngredientInfo(List<ResponseIngredientObject> ingredient_info) {
			this.ingredient_info = ingredient_info;
		}
	}

	public static class RequestProductFilter extends Api.RequestGetAllByPage {
		@NotNull
		public long brand;
		@NotNull
		public long[] ingredients;
	}

	public static Result product(long product_id) {
		ResponseState state = new ResponseState(session());

		Product result = Product.byId(product_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(product.render(state, result));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_ingredient_info() {
		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Product result = Product.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Product not found");
			}

			Set<Ingredient> ingredients = new HashSet<>();

			List<ProductIngredient> links = result.getIngredientLinks();

			for (ProductIngredient link : links) {
				if (link.getIngredient_name().getIngredient() != null) {
					ingredients.add(link.getIngredient_name().getIngredient());
				}
			}

			List<ResponseIngredientObject> results = new ArrayList<>();
			for (Ingredient ingredient : ingredients) {
				results.add(new ResponseIngredientObject(
						WordUtils.capitalizeFully(ingredient.getName()),
						ingredient.getDescription(),
						ingredient.getFunctionsString()
				));
			}

			Response response = new ResponseProductIngredientInfo(results);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product() {
		try {
			Api.RequestGetById request =
					Api.read(ctx(), Api.RequestGetById.class);

			Product result = Product.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Product not found");
			}

			Response response = new ResponseProductInfo(
					result.getName(),
					result.getBrandName(),
					result.getDescription()
			);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
/*
	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_filter() {
		try {
			RequestProductFilter request = Api.read(ctx(), RequestProductFilter.class);

			Page page = new Page(request.page, 20);
			List<Product> result = Product.byFunctions(request.functions, page);

			ResponseIngredientFilter response = new ResponseIngredientFilter();
			response.count = page.count;

			for (Ingredient ingredient : result) {
				response.results.add(new ResponseIngredientObject(
						ingredient.getId(),
						ingredient.getDisplayName(),
						ingredient.getCas_number(),
						ingredient.getDescription(),
						ingredient.getFunctionsString(),
						ingredient.getNamesString()
				));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}*/
}
