package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
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
import src.models.Permissible;
import src.models.data.*;

import java.util.ArrayList;
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
		public List<String> ingredients;
		@NotNull
		public List<String> key_ingredients;
	}

	public static Result api_product_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.PRODUCT.EDIT);

			RequestProductUpdate request = Api.read(ctx(), RequestProductUpdate.class);

			Product result = Product.byId(request.id);
			if (request.id == BaseModel.NEW_ID) {
				result = new Product();
			}
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			Brand brand = Brand.byId(request.brand_id);
			if (brand == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Brand id " + request.id + " not found");
			}

			result.setBrand(brand);
			result.setLine(request.line);
			result.setName(request.name);
			result.setDescription(request.description);

			DBFormat.DBCache cache = new DBFormat.DBCache();
			cache.cacheIngredientNames();

			List<ProductIngredient> ingredient_links = new ArrayList<>();

			if (request.id != BaseModel.NEW_ID) {
				for (ProductIngredient link : result.getIngredientLinks()) {
					link.delete();
				}
			}

			for (String ingredient : request.ingredients) {
				IngredientName name = cache.matchIngredientName(ingredient);
				ProductIngredient item = new ProductIngredient();
				item.setProduct(result);
				item.setIngredient_name(name);
				item.setIs_key(false);
				ingredient_links.add(item);
			}

			for (String ingredient : request.key_ingredients) {
				IngredientName name = cache.matchIngredientName(ingredient);
				ProductIngredient item = new ProductIngredient();
				item.setProduct(result);
				item.setIngredient_name(name);
				item.setIs_key(true);
				ingredient_links.add(item);
			}

			result.setIngredientLinks(ingredient_links);

			result.save();

			return Api.write(new InfoResponse("Product " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_brand_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			Api.RequestObjectUpdate request = Api.read(ctx(), Api.RequestObjectUpdate.class);

			Brand result = Brand.byId(request.id);
			if (request.id == BaseModel.NEW_ID) {
				result = new Brand();
			}
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Brand " + request.id + " not found");
			}

			result.setName(request.name);
			result.setDescription(request.description);

			result.save();

			return Api.write(new InfoResponse("Brand " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_type_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.INGREDIENT.EDIT);

			Api.RequestObjectUpdate request = Api.read(ctx(), Api.RequestObjectUpdate.class);

			ProductType result = ProductType.byId(request.id);
			if (request.id == BaseModel.NEW_ID) {
				result = new ProductType();
			}
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Product type " + request.id + " not found");
			}

			result.setName(request.name);
			result.setDescription(request.description);

			result.save();

			return Api.write(new InfoResponse("Product type " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
