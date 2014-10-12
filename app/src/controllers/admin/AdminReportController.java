package src.controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.Permissible;
import src.models.feedback.Report;

import java.util.ArrayList;
import java.util.List;

public class AdminReportController extends Controller {
	public static class ResponseReportList extends Response {
		public List<ResponseReportObject> result = new ArrayList<>();
	}

	public static class ResponseReportObject {
		public long id;
		public String path;
		public String type;
		public String title;
		public String content;
		public String reported_by;
		public long timestamp;

		public ResponseReportObject(long id, String path, String type,
		                            String title, String content,
		                            String reported_by, long timestamp) {
			this.id = id;
			this.path = path;
			this.type = type;
			this.title = title;
			this.content = content;
			this.reported_by = reported_by;
			this.timestamp = timestamp;
		}
	}

	public static Result api_report_list() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.REPORT.VIEW);

			Api.RequestGetAllByPage request = Api.read(ctx(), Api.RequestGetAllByPage.class);

			List<Report> reports = Report.all(request.page, 20);

			ResponseReportList response = new ResponseReportList();
			for (Report report : reports) {
				response.result.add(getResponse(report));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_report_byid() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.REPORT.VIEW);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Report result = Report.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}
			ResponseReportList response = new ResponseReportList();
			response.result.add(getResponse(result));

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_report_delete() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.REPORT.DELETE);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Report result = Report.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			result.delete();

			return Api.write(new InfoResponse("Successfully deleted report " + result.getTitle()));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_analytics() {
		ResponseState state = new ResponseState(session());

		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			//TODO

			return Api.write(new InfoResponse("Feedback received. Thank you!"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	private static ResponseReportObject getResponse(Report object) {
		return new ResponseReportObject(
				object.getId(),
				object.getPath(),
				object.getType(),
				object.getTitle(),
				object.getContent(),
				object.getUserName(),
				object.getTimestamp()
		);
	}

}
