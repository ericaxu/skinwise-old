package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.util.ResponseState;
import views.html.*;

public class HtmlController extends Controller {

	public static Result index() {
		ResponseState state = new ResponseState(session());
		return redirect("/products");
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

	public static Result routine() {
		ResponseState state = new ResponseState(session());
		return ok(routine.render(state));
	}

	public static Result about() {
		ResponseState state = new ResponseState(session());
		return ok(about.render(state));
	}

	public static Result team() {
		ResponseState state = new ResponseState(session());
		return ok(team.render(state));
	}
}
