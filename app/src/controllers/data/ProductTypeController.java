package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.util.ResponseState;
import src.models.data.ProductType;

public class ProductTypeController extends Controller {
	public static class ProductTypeObject extends Api.ResponseNamedModelObject {
		public int products;

		public ProductTypeObject(long id, String name, String description, int products) {
			super(id, name, description);
			this.products = products;
		}
	}

	public static Result product_type(long product_type_id) {
		ResponseState state = new ResponseState(session());

		ProductType result = App.cache().types.get(product_type_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(/*product_type.render(state, result)*/);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_type_byid() {
		return NamedDataController.api_named_model_byid(ctx(), App.cache().types, "Product Type", serializer);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_product_type_all() {
		return NamedDataController.api_named_model_all(App.cache().types, serializer);
	}

	private static final ProductTypeSerializer serializer = new ProductTypeSerializer();

	public static class ProductTypeSerializer extends NamedDataController.Serializer<ProductType> {
		@Override
		public Api.ResponseNamedModelObject create(ProductType object) {
			return new ProductTypeObject(
					object.getId(),
					object.getName(),
					object.getDescription(),
					object.getProducts().size()
			);
		}
	}
}
