package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class Application extends Controller {

	public static Result index() { return ok(index.render()); }

	public static Result product() {
		return ok(product.render());
	}

	public static Result ingredient() { return ok(ingredient.render()); }

	public static Result products() {return ok(products.render()); }

}
