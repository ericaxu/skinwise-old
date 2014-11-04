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
import src.models.MemCache;
import src.models.data.*;
import src.models.util.Page;
import views.html.product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductController extends Controller {
	public static class ResponseIngredientObject {
		public long id;
		public String name;
		public String description;
		public long[] functions;

		public ResponseIngredientObject(long id, String name, String description, long[] functions) {
			this.id = id;
			this.name = name;
			this.description = description;
			this.functions = functions;
		}
	}

	public static class ResponseProductIngredientInfo extends Response {
		public List<ResponseIngredientObject> results;

		public ResponseProductIngredientInfo(List<ResponseIngredientObject> results) {
			this.results = results;
		}
	}

	public static class RequestProductFilter extends Api.RequestGetAllByPage {
		@NotNull
		public long[] brands;
		@NotNull
		public long[] ingredients;
		@NotNull
		public long[] types;
		@NotNull
		public long[] neg_brands;
		@NotNull
		public long[] neg_ingredients;
	}

	public static class ResponseProductObject {
		public long id;
		public long brand;
		public String line;
		public String name;
		public String description;
		public String image;

		public ResponseProductObject(long id, long brand, String line, String name, String description, String image) {
			this.id = id;
			this.brand = brand;
			this.line = line;
			this.name = name;
			this.description = description;
			this.image = image;
		}
	}

	public static class ResponseProductPropertyObject {
		public long id;
		public long product_id;
		public String key;
		public String text_value;
		public double number_value;

		public ResponseProductPropertyObject(long id, long product_id, String key, String text_value, double number_value) {
			this.id = id;
			this.product_id = product_id;
			this.key = key;
			this.text_value = text_value;
			this.number_value = number_value;
		}
	}

	public static Result product(long product_id) {
		ResponseState state = new ResponseState(session());

		Product result = App.cache().products.get(product_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		result.incrementPopularity(1);

		return ok(product.render(state, result));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_byid() {
		try {
			Api.RequestGetById request =
					Api.read(ctx(), Api.RequestGetById.class);

			Product result = App.cache().products.get(request.id);
			Api.checkNotNull(result, "Product", request.id);

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = 1;

			response.results.add(new ResponseProductObject(
					result.getId(),
					result.getBrand_id(),
					result.getLine(),
					result.getName(),
					result.getDescription(),
					result.getImage()
			));

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

			for (long id : request.brands) {
				Brand object = App.cache().brands.get(id);
				Api.checkNotNull(object, "Brand", id);
			}

			for (long id : request.neg_brands) {
				Brand object = App.cache().brands.get(id);
				Api.checkNotNull(object, "Brand", id);
			}

			for (long id : request.types) {
				ProductType object = App.cache().types.get(id);
				Api.checkNotNull(object, "Product Type", id);
			}

			for (long id : request.ingredients) {
				Ingredient object = App.cache().ingredients.get(id);
				Api.checkNotNull(object, "Ingredient", id);
			}

			for (long id : request.neg_ingredients) {
				Ingredient object = App.cache().ingredients.get(id);
				Api.checkNotNull(object, "Ingredient", id);
			}

			Page page = new Page(request.page, 20);
			List<Product> result = Product.byFilter(
					request.brands, request.neg_brands,
					request.types,
					request.ingredients, request.neg_ingredients,
					false,
					page);

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = page.count;

			for (Product product : result) {
				response.results.add(new ResponseProductObject(
						product.getId(),
						product.getBrand_id(),
						product.getLine(),
						product.getName(),
						product.getDescription(),
						product.getImage()
				));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_ingredient_info() {
		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Product result = App.cache().products.get(request.id);
			Api.checkNotNull(result, "Product", request.id);

			Set<Ingredient> ingredients = new HashSet<>();

			List<ProductIngredient> links = result.getProductIngredients();

			for (ProductIngredient link : links) {
				if (link.getAlias().getIngredient() != null) {
					ingredients.add(link.getAlias().getIngredient());
				}
			}

			List<ResponseIngredientObject> results = new ArrayList<>();
			for (Ingredient ingredient : ingredients) {
				results.add(new ResponseIngredientObject(
						ingredient.getId(),
						WordUtils.capitalizeFully(ingredient.getName()),
						ingredient.getDescription(),
						ingredient.getFunctionIds().toArray()
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
	public static Result api_product_properties() {
		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			MemCache cache = App.cache();

			Product result = cache.products.get(request.id);
			Api.checkNotNull(result, "Product", request.id);

			List<ProductProperty> properties = cache.product_properties.getList(
					result.getProductProperties().toArray());

			Api.ResponseResultList response = new Api.ResponseResultList();

			for (ProductProperty property : properties) {
				response.results.add(new ResponseProductPropertyObject(
						property.getId(),
						property.getProduct_id(),
						property.getKey(),
						property.getText_value(),
						property.getNumber_value()
				));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
