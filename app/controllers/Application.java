package controllers;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import util.dbimport.INCI;
import views.html.*;

import java.io.IOException;

public class Application extends Controller {

	public static Result index() {
		return ok(index.render());
	}

	public static Result product() {
		return ok(product.render());
	}

	public static Result ingredient() {
		return ok(ingredient.render());
	}

	public static Result products() {
		return ok(products.render());
	}

	public static Result ingredients() {
		return ok(ingredients.render());
	}

	public static Result profile() {
		return ok(profile.render());
	}
}
