package src.controllers.data;

import org.apache.commons.lang3.text.WordUtils;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.Prettyfy;
import src.controllers.util.ResponseState;
import src.models.data.*;
import src.models.util.Page;
import views.html.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public static class RequestProductNumberPropertyFilter {
		public double min;
		public double max;
	}

	public static class RequestProductFilter extends Api.RequestGetAllByPage {
		public long[] brands;
		public long[] ingredients;
		public long[] types;
		public long[] benefits;
		public long[] neg_brands;
		public long[] neg_ingredients;
		public Map<String, RequestProductNumberPropertyFilter> number_properties;

		public void sanitize() {
			brands = sanitize(brands);
			ingredients = sanitize(ingredients);
			types = sanitize(types);
			benefits = sanitize(benefits);
			neg_brands = sanitize(neg_brands);
			neg_ingredients = sanitize(neg_ingredients);
			if (number_properties == null) {
				number_properties = new HashMap<>();
			}
		}
	}

	public static class ResponseProductPropertyObject {
		public long id;
		public long product_id;
		public String key;
		public String text_value;
		public double number_value;

		public ResponseProductPropertyObject(ProductProperty result) {
			this(
					result.getId(),
					result.getProduct_id(),
					result.getKey(),
					result.getText_value(),
					result.getNumber_value()
			);
		}

		public ResponseProductPropertyObject(long id, long product_id, String key, String text_value, double number_value) {
			this.id = id;
			this.product_id = product_id;
			this.key = key;
			this.text_value = text_value;
			this.number_value = number_value;
		}
	}

	public static class ResponseProductObject {
		public long id;
		public long brand;
		public String line;
		public String name;
		public String description;
		public String image;
		public Map<String, ResponseProductPropertyObject> properties = new HashMap<>();

		public ResponseProductObject(Product result) {
			this(
					result.getId(),
					result.getBrand_id(),
					result.getLine(),
					result.getName(),
					result.getDescription(),
					result.getImage()
			);

			List<ProductProperty> properties = App.cache().product_properties.getList(
					result.getProductProperties().toArray());

			for (ProductProperty property : properties) {
				this.properties.put(property.getKey(), new ResponseProductPropertyObject(property));
			}
		}

		private ResponseProductObject(long id, long brand, String line, String name,
		                              String description, String image) {
			this.id = id;
			this.brand = brand;
			this.line = line;
			this.name = name;
			this.description = description;
			this.image = image;
		}
	}

	public static Result product(long product_id) {
		ResponseState state = new ResponseState(session());

		Product result = App.cache().products.get(product_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		result.incrementPopularity(1);

		return ok(Prettyfy.prettify(product.render(state, result)));
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

			response.results.add(new ResponseProductObject(result));

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
			request.sanitize();

			for (long id : request.brands) {
				Brand object = App.cache().brands.get(id);
				Api.checkNotNull(object, "Brand", id);
			}

			for (long id : request.neg_brands) {
				Brand object = App.cache().brands.get(id);
				Api.checkNotNull(object, "Brand", id);
			}

			for (long id : request.types) {
				Type object = App.cache().types.get(id);
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

			for (long id : request.benefits) {
				Benefit object = App.cache().benefits.get(id);
				Api.checkNotNull(object, "Benefit", id);
			}

			List<Product.ProductPropertyNumberFilter> numberFilters = new ArrayList<>();

			for (Map.Entry<String, RequestProductNumberPropertyFilter> entry :
					request.number_properties.entrySet()) {
				numberFilters.add(new Product.ProductPropertyNumberFilter(
						entry.getKey(), entry.getValue().min, entry.getValue().max));
			}

			Page page = new Page(request.page, 20);
			List<Product> result = Product.byFilter(
					request.brands, request.neg_brands,
					request.types, request.benefits,
					request.ingredients, request.neg_ingredients,
					numberFilters, null,
					false,
					page);

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = page.count;

			for (Product product : result) {
				response.results.add(new ResponseProductObject(product));
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

			List<Ingredient> ingredients = new ArrayList<>();

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
						ingredient.getDisplay_name(),
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
	public static Result api_product_similar() {
		try {
			Api.RequestGetById request =
					Api.read(ctx(), Api.RequestGetById.class);

			Page page = new Page(0, 20);
			List<Product> result = Product.byFilter(
					new long[]{},
					new long[]{},
					new long[]{},
					new long[]{},
					new long[]{},
					new long[]{},
					null, null,
					false,
					page
			);

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = page.count;

			for (Product product : result) {
				response.results.add(new ResponseProductObject(product));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
