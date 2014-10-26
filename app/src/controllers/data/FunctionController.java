package src.controllers.data;

import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.util.ResponseState;
import src.models.data.Function;
import views.html.function;

public class FunctionController extends Controller {
	public static Result function(long function_id) {
		ResponseState state = new ResponseState(session());

		Function result = App.cache().functions.get(function_id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(function.render(state, result));
	}
}
