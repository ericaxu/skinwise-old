package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.util.ResponseState;
import views.html.*;

public class HtmlController extends Controller {

	public static Result index() {
		ResponseState state = new ResponseState(session());
		return ok(index.render(state));
	}

	public static Result product() {
		ResponseState state = new ResponseState(session());
		return ok(product.render(state));
	}

	public static Result ingredient() {
		ResponseState state = new ResponseState(session());
		return ok(ingredient.render(state));
	}

	public static Result products() {
		ResponseState state = new ResponseState(session());
		return ok(products.render(state));
	}

	public static Result ingredients() {
		ResponseState state = new ResponseState(session());
		return ok(ingredients.render(state));
	}

	public static Result profile() {
		ResponseState state = new ResponseState(session());
		return ok(profile.render(state));
	}
}
