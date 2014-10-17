package src.controllers.feedback;

import org.hibernate.validator.constraints.NotEmpty;
import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.util.ResponseState;
import src.models.feedback.Report;

public class ReportController extends Controller {
	public static class RequestReport extends Request {
		@NotNull
		public String path;
		@NotEmpty
		public String type;
		@NotEmpty
		public String content;
	}

	public static Result api_report_create() {
		ResponseState state = new ResponseState(session());

		try {
			RequestReport request = Api.read(ctx(), RequestReport.class);

			Report report = new Report();
			report.setPath(request.path);
			report.setType(request.type);
			report.setContent(request.content);
			report.setUser(state.getUser());
			report.setTimestamp(System.currentTimeMillis());
			report.setResolved(false);

			report.save();

			return Api.write(new InfoResponse("Feedback received. Thank you!"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
