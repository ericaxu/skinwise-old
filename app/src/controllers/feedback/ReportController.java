package src.controllers.feedback;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;

public class ReportController extends Controller {
	public static Result api_report_create() {
		return Api.write();
	}
}
