package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.util.Prettyfy;
import src.controllers.util.ResponseState;
import views.html.*;

public class HtmlController extends Controller {

	public static Result index() {
		ResponseState state = new ResponseState(session());
		return redirect("/products");
	}

	public static Result products() {
		ResponseState state = new ResponseState(session());
		return ok(Prettyfy.prettify(products.render(state)));
	}

	public static Result ingredients() {
		ResponseState state = new ResponseState(session());
		return ok(Prettyfy.prettify(ingredients.render(state)));
	}

	public static Result compare() {
		ResponseState state = new ResponseState(session());
		return ok(Prettyfy.prettify(compare.render(state)));
	}

	public static Result profile() {
		ResponseState state = new ResponseState(session());
		return ok(Prettyfy.prettify(profile.render(state)));
	}

	public static Result routine() {
		ResponseState state = new ResponseState(session());
		return ok(Prettyfy.prettify(routine.render(state)));
	}

	public static Result about() {
		ResponseState state = new ResponseState(session());
		return ok(Prettyfy.prettify(about.render(state)));
	}

	public static Result team() {
		ResponseState state = new ResponseState(session());
		return ok(Prettyfy.prettify(team.render(state)));
	}
}
