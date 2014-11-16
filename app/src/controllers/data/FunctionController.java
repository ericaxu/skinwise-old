package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.util.Prettyfy;
import src.controllers.util.ResponseState;
import src.models.data.Function;
import views.html.function;

public class FunctionController extends Controller {
	public static class FunctionObject extends Api.ResponseNamedModelObject {
		public int ingredient_count;

		public FunctionObject(long id, String name, String description, int ingredient_count) {
			super(id, name, description);
			this.ingredient_count = ingredient_count;
		}
	}

	public static Result function(long function_id) {
		ResponseState state = new ResponseState(session());

		Function result = App.cache().functions.get(function_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(Prettyfy.prettify(function.render(state, result)));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_function_byid() {
		return NamedDataController.api_named_model_byid(ctx(), App.cache().functions, "Function", serializer);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_function_all() {
		return NamedDataController.api_named_model_all(App.cache().functions, serializer);
	}

	private static final FunctionSerializer serializer = new FunctionSerializer();

	public static class FunctionSerializer extends NamedDataController.Serializer<Function> {
		@Override
		public Api.ResponseNamedModelObject create(Function object) {
			return new FunctionObject(
					object.getId(),
					object.getName(),
					object.getDescription(),
					object.getIngredients().size()
			);
		}
	}
}
