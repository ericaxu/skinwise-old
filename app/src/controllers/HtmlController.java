package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class HtmlController extends Controller {

	public static Result index() {
		return ok(index.render(null));
	}

	public static Result product() {
		return ok(product.render(null));
	}

	public static Result ingredient() {
		return ok(ingredient.render(null));
	}

	public static Result products() {
		return ok(products.render(null));
	}

	public static Result ingredients() {
		return ok(ingredients.render(null));
	}

	public static Result profile() {
		return ok(profile.render(null));
	}
}
