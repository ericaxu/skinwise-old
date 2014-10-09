package src.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.error404;

public class ErrorController extends Controller {
	public static Result notfound() {
		return ok(error404.render(null));
	}
}
