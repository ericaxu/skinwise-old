package src.controllers.feedback;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.util.ResponseState;
import src.models.feedback.Analytics;

public class AnalyticsController extends Controller {
	public static class RequestAnalytics extends Request {
		RequestAnalyticsEvent[] events;
	}

	public static class RequestAnalyticsEvent {
		public String event;
		public String summary;
		public String data;
	}

	public static Result api_analytics() {
		ResponseState state = new ResponseState(session());

		try {
			RequestAnalytics request = Api.read(ctx(), RequestAnalytics.class);

			for (RequestAnalyticsEvent event : request.events) {
				Analytics analytics = new Analytics();
				analytics.setEvent(event.event);
				analytics.setSummary(event.summary);
				analytics.setData(event.data);
				analytics.setTimestamp(System.currentTimeMillis());
				analytics.setUser(state.getUser());

				analytics.save();
			}

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
