package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.util.ResponseState;
import src.models.data.Brand;
import views.html.brand;

public class BrandController extends Controller {
	public static class BrandObject extends Api.ResponseNamedModelObject {
		public int products;

		public BrandObject(long id, String name, String description, int products) {
			super(id, name, description);
			this.products = products;
		}
	}

	public static Result brand(long brand_id) {
		ResponseState state = new ResponseState(session());

		Brand result = App.cache().brands.get(brand_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(brand.render(state, result));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_brand_byid() {
		return NamedDataController.api_named_model_byid(ctx(), App.cache().brands, "Brand", serializer);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_brand_all() {
		return NamedDataController.api_named_model_all(App.cache().brands, serializer);
	}

	private static final BrandSerializer serializer = new BrandSerializer();

	public static class BrandSerializer extends NamedDataController.Serializer<Brand> {
		@Override
		public Api.ResponseNamedModelObject create(Brand object) {
			return new BrandObject(
					object.getId(),
					object.getName(),
					object.getDescription(),
					object.getProducts().size()
			);
		}
	}
}
