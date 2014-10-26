package src.controllers.admin;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.feedback.Report;
import src.models.user.Permissible;
import src.models.util.Page;

import java.util.ArrayList;
import java.util.List;

public class AdminReportController extends Controller {
	public static class ResponseReportObject {
		public long id;
		public String path;
		public String type;
		public String email;
		public String content;
		public String reported_by;
		public boolean resolved;
		public long timestamp;

		public ResponseReportObject(long id, String path,
		                            String type, String email,
		                            String content, String reported_by,
		                            boolean resolved, long timestamp) {
			this.id = id;
			this.path = path;
			this.type = type;
			this.email = email;
			this.content = content;
			this.reported_by = reported_by;
			this.resolved = resolved;
			this.timestamp = timestamp;
		}
	}

	public static class ResponseReportList extends Response {
		public List<ResponseReportObject> results = new ArrayList<>();
	}

	public static class RequestReports extends Api.RequestGetAllByPage {
		public boolean resolved;
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_report_list() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.REPORT.VIEW);

			RequestReports request = Api.read(ctx(), RequestReports.class);

			List<Report> reports = Report.all(new Page(request.page), request.resolved);

			ResponseReportList response = new ResponseReportList();
			for (Report report : reports) {
				response.results.add(getResponse(report));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
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
			response.results.add(getResponse(result));

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_report_resolve() {
		ResponseState state = new ResponseState(session());

		try {
			state.requirePermission(Permissible.REPORT.RESOLVE);

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Report result = Report.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Report " + request.id + " not found");
			}

			result.setResolved(true);
			result.save();

			return Api.write(new InfoResponse("Report " + request.id + " resolved"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
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
				object.getEmail(),
				object.getContent(),
				object.getUserName(),
				object.isResolved(),
				object.getTimestamp()
		);
	}
}
