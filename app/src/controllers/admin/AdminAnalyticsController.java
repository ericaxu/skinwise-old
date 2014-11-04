package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.feedback.Analytics;
import src.models.user.Permissible;
import src.models.user.User;
import src.models.util.Page;

import java.util.List;

public class AdminAnalyticsController extends Controller {
	public static Result api_analytics_by_user() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.ADMIN.ANALYTICS);

			Api.RequestGetByIdAndPage request = Api.read(ctx(), Api.RequestGetByIdAndPage.class);

			User result = User.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			List<Analytics> list = Analytics.byUser(request.id, new Page(request.page));

			//TODO

			return Api.write();
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
