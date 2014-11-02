package src.controllers.userdata;

import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.data.Ingredient;
import src.models.data.Product;
import src.models.user.User;
import src.models.userdata.UserPreference;
import src.util.Json;
import src.util.Logger;

import java.io.IOException;

public class UserPreferenceController extends Controller {
	private static final String TAG = "UserPreferenceController";
	private static final String INGREDIENTS = "ingredients";
	private static final String PRODUCTS = "products";

	public static class RequestSetList extends Request {
		@NotNull
		public long[] ids;
	}

	public static class ResposeGetList extends Response {
		public long[] ids;
	}

	private static class LongListFormat {
		public long[] ids;
	}

	public static Result api_pref_set_ingredients() {
		ResponseState state = new ResponseState(session());

		try {
			checkLoggedIn(state);

			RequestSetList request = Api.read(ctx(), RequestSetList.class);

			for (long id : request.ids) {
				Ingredient object = App.cache().ingredients.get(id);
				Api.checkNotNull(object, "Ingredient", id);
			}

			LongListFormat dbObject = new LongListFormat();
			dbObject.ids = request.ids;

			set_list(state.getUser(), INGREDIENTS, dbObject);

			return Api.write(new InfoResponse("Ingredient list saved"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_pref_get_ingredients() {
		ResponseState state = new ResponseState(session());

		try {
			checkLoggedIn(state);

			ResposeGetList response = new ResposeGetList();
			response.ids = get_list(state.getUser(), INGREDIENTS);
			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_pref_set_products() {
		ResponseState state = new ResponseState(session());

		try {
			checkLoggedIn(state);

			RequestSetList request = Api.read(ctx(), RequestSetList.class);

			for (long id : request.ids) {
				Product object = App.cache().products.get(id);
				Api.checkNotNull(object, "Product", id);
			}

			LongListFormat dbObject = new LongListFormat();
			dbObject.ids = request.ids;

			set_list(state.getUser(), PRODUCTS, dbObject);

			return Api.write(new InfoResponse("Product list saved"));
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_pref_get_products() {
		ResponseState state = new ResponseState(session());

		try {
			checkLoggedIn(state);

			ResposeGetList response = new ResposeGetList();
			response.ids = get_list(state.getUser(), PRODUCTS);
			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	private static long[] get_list(User user, String key) {

		UserPreference result = UserPreference.byUserAndKey(user, key);

		if (result != null) {
			try {
				LongListFormat dbObject = Json.deserialize(result.getValue(), LongListFormat.class);
				return dbObject.ids;
			}
			catch (IOException e) {
				Logger.error(TAG, e);
			}
		}

		return new long[0];
	}

	private static void set_list(User user, String key, LongListFormat dbObject) {
		String value = Json.serialize(dbObject);

		UserPreference result = UserPreference.byUserAndKey(user, key);
		if (result == null) {
			result = new UserPreference();
			result.setUser(user);
			result.setKey(key);
		}
		result.setValue(value);
		result.save();
	}

	private static void checkLoggedIn(ResponseState state) throws BadRequestException {
		if (state.getUser() == null) {
			throw new BadRequestException(Response.NOT_FOUND, "Not logged in");
		}
	}
}