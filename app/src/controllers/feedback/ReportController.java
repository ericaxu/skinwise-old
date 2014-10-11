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
		public String title;
		@NotEmpty
		public String content;
	}

	public static class RequestAnalyticsEvent {
		public String event;
		public String data;
		public long timestamp;
	}

	public static class RequestAnalytics extends Request {
		RequestAnalyticsEvent[] events;
	}

	public static Result api_report_create() {
		ResponseState state = new ResponseState(session());

		try {
			RequestReport request = Api.read(ctx(), RequestReport.class);

			Report report = new Report();
			report.setPath(request.path);
			report.setType(request.type);
			report.setTitle(request.title);
			report.setContent(request.content);
			report.setReported_by(state.getUser());
			report.setTimestamp(System.currentTimeMillis());

			report.save();

			return Api.write(new InfoResponse("Feedback received. Thank you!"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_analytics() {
		ResponseState state = new ResponseState(session());

		try {
			RequestAnalytics request = Api.read(ctx(), RequestAnalytics.class);

			//TODO

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
