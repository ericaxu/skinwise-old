package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.util.ResponseState;
import src.models.data.Type;
import views.html.product_type;

public class TypeController extends Controller {
	public static class TypeObject extends Api.ResponseNamedModelObject {
		public int product_count;

		public TypeObject(long id, String name, String description, int product_count) {
			super(id, name, description);
			this.product_count = product_count;
		}
	}

	public static Result product_type(long product_type_id) {
		ResponseState state = new ResponseState(session());

		Type result = App.cache().types.get(product_type_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(product_type.render(state, result));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_type_byid() {
		return NamedDataController.api_named_model_byid(ctx(), App.cache().types, "Type", serializer);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_type_all() {
		return NamedDataController.api_named_model_all(App.cache().types, serializer);
	}

	private static final TypeSerializer serializer = new TypeSerializer();

	public static class TypeSerializer extends NamedDataController.Serializer<Type> {
		@Override
		public Api.ResponseNamedModelObject create(Type object) {
			return new TypeObject(
					object.getId(),
					object.getName(),
					object.getDescription(),
					object.getProducts().size()
			);
		}
	}
}
