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
import src.models.MemCache;
import src.models.data.Brand;
import src.models.data.Product;
import src.models.data.ProductProperty;
import src.models.data.ProductType;
import src.models.user.Permissible;
import src.models.util.BaseModel;
import src.util.Util;

public class AdminProductController extends Controller {
	private static final String TAG = "AdminProductController";

	public static class RequestProductUpdate extends Request {
		public long id;
		public long brand_id;
		public long popularity;
		@NotNull
		public String line;
		@NotEmpty
		public String name;
		@NotNull
		public String description;
		@NotNull
		public String image;
		public String price;
		public float size;
		public String size_unit;
		//		@NotNull
		//		public String ingredients;
		//		@NotNull
		//		public String key_ingredients;
	}

	public static class RequestProductPropertyUpdate extends Request {
		public long id;
		public long product_id;
		@NotEmpty
		public String key;
		public String text_value;
		public double number_value;
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.PRODUCT.EDIT);

			RequestProductUpdate request = Api.read(ctx(), RequestProductUpdate.class);

			MemCache cache = App.cache();

			Product result;
			if (request.id == BaseModel.NEW_ID) {
				result = new Product();
			}
			else {
				result = cache.products.get(request.id);
				Api.checkNotNull(result, "Product", request.id);
			}

			Brand brand = cache.brands.get(request.brand_id);
			Api.checkNotNull(brand, "Brand", request.brand_id);
			long price;
			try {
				price = Util.parsePrice(request.price);
			}
			catch (NumberFormatException e) {
				throw new BadRequestException(Response.INVALID, "Bad price");
			}

			//			List<Alias> ingredients = cache.matcher.matchAllAliases(request.ingredients);
			//			List<Alias> key_ingredients = cache.matcher.matchAllAliases(request.key_ingredients);

			if (request.size_unit == null) {
				request.size_unit = "";
			}

			String oldName = result.getName();
			long oldBrandId = result.getBrand_id();

			synchronized (result) {
				result.setName(request.name);
				result.setBrand(brand);
				result.setLine(request.line);
				result.setDescription(request.description);
				result.setImage(request.image);
				result.setPopularity(request.popularity);

				result.setPrice(price);
				result.setSize(request.size);
				result.setSize_unit(request.size_unit);

				result.save();

				cache.products.update(result, oldBrandId, oldName);

				//result.saveIngredients(ingredients, key_ingredients);
			}

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
			state.requirePermission(Permissible.PRODUCT.EDIT);

			Api.RequestObjectUpdate request = Api.read(ctx(), Api.RequestObjectUpdate.class);

			MemCache cache = App.cache();

			//Name uniqueness
			long other_id = BaseModel.getIdIfExists(cache.brands.get(request.name));
			if (other_id != request.id) {
				throw new BadRequestException(Response.INVALID, "Brand " + request.name + " already exists");
			}

			Brand result;
			if (request.id == BaseModel.NEW_ID) {
				result = new Brand();
			}
			else {
				result = cache.brands.get(request.id);
				Api.checkNotNull(result, "Brand", request.id);
			}

			String oldName = result.getName();

			synchronized (result) {
				result.setName(request.name);
				result.setDescription(request.description);
				result.save();

				cache.brands.update(result, oldName);
			}

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
			state.requirePermission(Permissible.PRODUCT.EDIT);

			Api.RequestObjectUpdate request = Api.read(ctx(), Api.RequestObjectUpdate.class);

			MemCache cache = App.cache();

			//Name uniqueness
			long other_id = BaseModel.getIdIfExists(cache.types.get(request.name));
			if (other_id != request.id) {
				throw new BadRequestException(Response.INVALID, "Product Type " + request.name + " already exists");
			}

			ProductType result;
			if (request.id == BaseModel.NEW_ID) {
				result = new ProductType();
			}
			else {
				result = cache.types.get(request.id);
				Api.checkNotNull(result, "Product Type", request.id);
			}

			String oldName = result.getName();

			synchronized (result) {
				result.setName(request.name);
				result.setDescription(request.description);
				result.save();

				cache.types.update(result, oldName);
			}

			return Api.write(new InfoResponse("Product type " + result.getName() + " updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_property_update() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.PRODUCT.EDIT);

			RequestProductPropertyUpdate request = Api.read(ctx(), RequestProductPropertyUpdate.class);

			MemCache cache = App.cache();

			Product product = cache.products.get(request.product_id);
			Api.checkNotNull(product, "Product", request.product_id);

			ProductProperty result;
			if (request.id == BaseModel.NEW_ID) {
				result = new ProductProperty();
			}
			else {
				result = cache.product_properties.get(request.id);
				Api.checkNotNull(result, "Product Property", request.id);
			}

			synchronized (result) {
				result.setKey(request.key);
				result.setProduct_id(request.product_id);
				result.setText_value(request.text_value);
				result.setNumber_value(request.number_value);
				result.save();

				cache.product_properties.update(result);
			}

			return Api.write(new InfoResponse("Product Property updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_property_delete() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.PRODUCT.EDIT);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			MemCache cache = App.cache();

			ProductProperty result = cache.product_properties.get(request.id);
			Api.checkNotNull(result, "Product Property", request.id);

			synchronized (result) {
				result.delete();

				cache.product_properties.remove(result);
			}

			return Api.write(new InfoResponse("Product Property updated"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
