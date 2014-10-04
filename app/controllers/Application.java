package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.product;
import views.html.ingredient;

public class Application extends Controller {

	public static Result index() { return ok(index.render()); }

	public static Result product() {
		return ok(product.render());
	}

	public static Result ingredient() { return ok(ingredient.render()); }

}
