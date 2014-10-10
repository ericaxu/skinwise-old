package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.views.RenderState;
import views.html.*;

public class HtmlController extends Controller {

	public static Result index() {
		RenderState state = new RenderState(session());
		return ok(index.render(state));
	}

	public static Result product() {

		RenderState state = new RenderState(session());
		return ok(product.render(state));
	}

	public static Result ingredient() {
		RenderState state = new RenderState(session());
		return ok(ingredient.render(state));
	}

	public static Result products() {
		RenderState state = new RenderState(session());
		return ok(products.render(state));
	}

	public static Result ingredients() {
		RenderState state = new RenderState(session());
		return ok(ingredients.render(state));
	}

	public static Result profile() {
		RenderState state = new RenderState(session());
		return ok(profile.render(state));
	}
}
