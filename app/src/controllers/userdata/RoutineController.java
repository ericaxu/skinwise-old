package src.controllers.userdata;

import play.mvc.Controller;
import play.mvc.Result;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotEmpty;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.BaseModel;
import src.models.data.Product;
import src.models.userdata.Routine;
import src.models.userdata.RoutineItem;

import java.util.ArrayList;
import java.util.List;

public class RoutineController extends Controller {
	public static class RequestRoutineUpdate extends Request {
		@NotEmpty
		public String name;
		public boolean is_public;
		public long id;
		public long[] product_ids;
	}

	public static class ResponseProductObject {
		public String brand;
		public String name;
	}

	public static class ResponseRoutineObject {
		@NotEmpty
		public String name;
		public boolean is_public;
		public long id;
		public List<ResponseProductObject> products = new ArrayList<>();
	}

	public static class ResponseRoutineList extends Response {
		public List<ResponseRoutineObject> result = new ArrayList<>();
	}

	public static Result api_routine_byuser() {
		ResponseState state = new ResponseState(session());

		try {
			if (state.getUser() == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Not logged in");
			}

			List<Routine> result = Routine.byUser(state.getUser());

			ResponseRoutineList response = new ResponseRoutineList();

			for (Routine routine : result) {
				ResponseRoutineObject routineObject = new ResponseRoutineObject();
				routineObject.id = routine.getId();
				routineObject.is_public = routine.isIs_public();
				routineObject.name = routine.getName();
				for (RoutineItem item : routine.getItems()) {
					ResponseProductObject productObject = new ResponseProductObject();
					productObject.brand = item.getProduct().getBrandName();
					productObject.name = item.getProduct().getName();
					routineObject.products.add(productObject);
				}
				response.result.add(routineObject);
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_routine_update() {
		ResponseState state = new ResponseState(session());

		try {
			if (state.getUser() == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Not logged in");
			}

			RequestRoutineUpdate request = Api.read(ctx(), RequestRoutineUpdate.class);

			Routine result = Routine.byId(request.id);
			if (request.id == BaseModel.NEW_ID) {
				result = new Routine();
			}
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Id " + request.id + " not found");
			}

			List<RoutineItem> items = new ArrayList<>();
			for (long product_id : request.product_ids) {
				Product product = Product.byId(product_id);
				if (product == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Product " + product_id + " not found");
				}
				RoutineItem item = new RoutineItem();
				item.setRoutine(result);
				item.setProduct(product);
				items.add(item);
			}

			for (RoutineItem item : result.getItems()) {
				item.delete();
			}

			result.setName(request.name);
			result.setIs_public(request.is_public);
			result.setItems(items);

			result.save();

			return Api.write(new InfoResponse("Routine saved"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_routine_delete() {
		ResponseState state = new ResponseState(session());

		try {
			if (state.getUser() == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Not logged in");
			}

			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Routine result = Routine.byId(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Routine not found");
			}

			if (!result.getUser().equals(state.getUser())) {
				throw new BadRequestException(Response.NOT_FOUND, "Routine not found");
			}

			for (RoutineItem item : result.getItems()) {
				item.delete();
			}
			result.delete();

			return Api.write(new InfoResponse("Routine deleted"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}
