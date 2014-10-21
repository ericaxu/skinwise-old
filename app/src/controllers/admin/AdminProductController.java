package src.controllers.admin;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotEmpty;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.BaseModel;
import src.models.MemCache;
import src.models.Permissible;
import src.models.data.Brand;
import src.models.data.IngredientName;
import src.models.data.Product;
import src.models.data.ProductType;

import java.util.List;

public class AdminProductController extends Controller {
	private static final String TAG = "AdminProductController";

	public static class RequestProductUpdate extends Request {
		public long id;
		public long brand_id;
		@NotNull
		public String line;
		@NotEmpty
		public String name;
		@NotNull
		public String description;
		@NotNull
		public String ingredients;
		@NotNull
		public String key_ingredients;
		@NotNull
		public String image;
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.PRODUCT.EDIT);

			RequestProductUpdate request = Api.read(ctx(), RequestProductUpdate.class);

			MemCache cache = App.cache();

			Product result = Product.byId(request.id);
			if (request.id == BaseModel.NEW_ID) {
				result = new Product();
			}
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			Brand brand = cache.brands.get(request.brand_id);
			if (brand == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Brand id " + request.id + " not found");
			}

			result.setLine(request.line);
			result.setDescription(request.description);
			result.setImage(request.image);

			cache.matcher.cache(cache.ingredient_names.all());
			List<IngredientName> ingredients = cache.matcher.matchAllIngredientNames(request.ingredients);
			List<IngredientName> key_ingredients = cache.matcher.matchAllIngredientNames(request.key_ingredients);
			cache.matcher.clear();

			result.setIngredientList(ingredients, key_ingredients);

			cache.products.updateAndSave(result, brand, request.name);

			return Api.write(new InfoResponse("Product " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_brand_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			Api.RequestObjectUpdate request = Api.read(ctx(), Api.RequestObjectUpdate.class);

			Brand result;
			if (request.id == BaseModel.NEW_ID) {
				result = new Brand();
			}
			else {
				result = Brand.byId(request.id);
				if (result == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Brand " + request.id + " not found");
				}
			}

			result.setDescription(request.description);

			App.cache().brands.updateNameAndSave(result, request.name);

			return Api.write(new InfoResponse("Brand " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_type_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			Api.RequestObjectUpdate request = Api.read(ctx(), Api.RequestObjectUpdate.class);

			ProductType result;
			if (request.id == BaseModel.NEW_ID) {
				result = new ProductType();
			}
			else {
				result = ProductType.byId(request.id);
				if (result == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Product type " + request.id + " not found");
				}
			}

			result.setDescription(request.description);

			App.cache().types.updateNameAndSave(result, request.name);

			return Api.write(new InfoResponse("Product type " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
