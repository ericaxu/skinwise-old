package src.controllers.userdata;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.userdata.Tracking;

import java.util.List;

public class TrackingController extends Controller {
	public static class RequestTrackingByTimeRange extends Request {
		public long timestamp_start;
		public long timestamp_end;
	}

	public static class RequestTrackingUpdate extends Request {
		public long timestamp;
		@NotNull
		public String data;
	}

	public static class ResponseTrackingObject {
		public long timestamp;
		@NotNull
		public String data;
	}

	public static Result api_tracking_update() {
		ResponseState state = new ResponseState(session());

		try {
			if (state.getUser() == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Not logged in");
			}

			RequestTrackingUpdate request = Api.read(ctx(), RequestTrackingUpdate.class);

			Tracking result = Tracking.byUserAndDate(state.getUser(), request.timestamp);
			if (result == null) {
				result = new Tracking();
				result.setTimestamp(request.timestamp);
				result.setUser(state.getUser());
			}

			result.setData(request.data);
			result.save();

			return Api.write(new InfoResponse("Tracked"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_tracking_by_range() {
		ResponseState state = new ResponseState(session());

		try {
			if (state.getUser() == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Not logged in");
			}

			RequestTrackingByTimeRange request = Api.read(ctx(), RequestTrackingByTimeRange.class);

			List<Tracking> result = Tracking.byUserAndDateRange(state.getUser(),
					request.timestamp_start, request.timestamp_end);

			Api.ResponseResultList response = new Api.ResponseResultList();

			for (Tracking tracking : result) {
				ResponseTrackingObject object = new ResponseTrackingObject();
				object.timestamp = tracking.getTimestamp();
				object.data = tracking.getData();
				response.results.add(object);
			}

			response.count = result.size();

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
