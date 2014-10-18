package src.controllers.data;

import org.apache.commons.lang3.text.WordUtils;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotNull;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.Page;
import src.models.data.*;
import views.html.product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductController extends Controller {
	public static class ResponseProduct extends Response {
		public String brand;
		public String type;
		public String name;
		public String description;

		public ResponseProduct(String brand, String type, String name, String description) {
			this.brand = brand;
			this.type = type;
			this.name = name;
			this.description = description;
		}
	}

	public static class ResponseIngredientObject {
		public long id;
		public String name;
		public String description;
		public List<String> functions;

		public ResponseIngredientObject(long id, String name, String description, List<String> functions) {
			this.id = id;
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
		public long[] brands;
		@NotNull
		public long[] ingredients;
	}

	public static class ResponseProductObject {
		public long id;
		public String name;
		public String brand;
		public String line;
		public String image;

		public ResponseProductObject(long id, String name, String brand, String line, String image) {
			this.id = id;
			this.name = name;
			this.brand = brand;
			this.line = line;
			this.image = image;
		}
	}

	public static class ResponseProductFilter extends Response {
		public List<ResponseProductObject> results = new ArrayList<>();
		public int count;
	}

	public static class ResponseBrandObject {
		public long id;
		public String name;
		public String description;

		public ResponseBrandObject(long id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}
	}

	public static class ResponseBrands extends Response {
		public List<ResponseBrandObject> results = new ArrayList<>();
	}

	public static class ResponseTypeObject {
		public long id;
		public String name;
		public String description;

		public ResponseTypeObject(long id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}
	}

	public static class ResponseTypes extends Response {
		public List<ResponseTypeObject> results = new ArrayList<>();
	}

	public static Result product(long product_id) {
		ResponseState state = new ResponseState(session());

		Product result = App.cache().products.get(product_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(product.render(state, result));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_byid() {
		try {
			Api.RequestGetById request =
					Api.read(ctx(), Api.RequestGetById.class);

			Product result = App.cache().products.get(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Product not found");
			}

			Response response = new ResponseProduct(
					result.getBrandName(),
					result.getTypeName(),
					result.getName(),
					result.getDescription()
			);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_filter() {
		try {
			RequestProductFilter request = Api.read(ctx(), RequestProductFilter.class);

			for (long brand_id : request.brands) {
				Brand brand = App.cache().brands.get(brand_id);
				if (brand == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Brand not found");
				}
			}

			for (long ingredient_id : request.ingredients) {
				Ingredient ingredient = App.cache().ingredients.get(ingredient_id);
				if (ingredient == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Ingredient not found");
				}
			}

			Page page = new Page(request.page, 20);
			List<Product> result = Product.byFilter(request.brands, request.ingredients, page);

			ResponseProductFilter response = new ResponseProductFilter();
			response.count = page.count;

			for (Product product : result) {
				response.results.add(new ResponseProductObject(
						product.getId(),
						product.getName(),
						product.getBrandName(),
						product.getLine(),
						""
				));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_brands() {
		List<Brand> result = Brand.all();

		ResponseBrands response = new ResponseBrands();

		for (Brand brand : result) {
			response.results.add(new ResponseBrandObject(
					brand.getId(),
					brand.getName(),
					brand.getDescription()
			));
		}

		return Api.write(response);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_types() {
		List<ProductType> result = ProductType.all();

		ResponseTypes response = new ResponseTypes();

		for (ProductType type : result) {
			response.results.add(new ResponseTypeObject(
					type.getId(),
					type.getName(),
					type.getDescription()
			));
		}

		return Api.write(response);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_ingredient_info() {
		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Product result = App.cache().products.get(request.id);
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
						ingredient.getId(),
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
}
