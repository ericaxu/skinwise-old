package src.controllers.userdata;

import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotEmpty;
import src.controllers.api.request.NotNull;
import src.controllers.api.request.Request;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.InfoResponse;
import src.controllers.api.response.Response;
import src.controllers.data.IngredientController;
import src.controllers.util.ResponseState;
import src.models.data.Ingredient;
import src.models.data.Product;
import src.models.user.User;
import src.models.userdata.UserPreference;
import src.util.Json;
import src.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserPreferenceController extends Controller {
	private static final String TAG = "UserPreferenceController";
	public static final String INGREDIENTS_WORKING = "ingredient_working";
	public static final String INGREDIENTS_NOT_WORKING = "ingredient_not_working";
	public static final String INGREDIENTS_BAD_REACTION = "ingredient_bad_reaction";


	public static class RequestSetList extends Request {
		@NotEmpty
		public String key;
		@NotNull
		public long[] ids;
	}

	public static class RequestGetList extends Request {
		@NotEmpty
		public String key;
	}

	public static class ResponseGetList extends Response {
		@NotNull
		public List<IngredientController.ResponseIngredientObject> results;
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

			if (request.key.equals(INGREDIENTS_WORKING) ||
					request.key.equals(INGREDIENTS_NOT_WORKING) ||
					request.key.equals(INGREDIENTS_BAD_REACTION)) {
					set_list(state.getUser(), request.key, dbObject);
				return Api.write(new InfoResponse("Preference saved."));
			} else {
				return Api.write(new ErrorResponse(Response.INVALID, "Set list key " + request.key + " is not valid."));
			}
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static Result api_pref_get_ingredients() {
		ResponseState state = new ResponseState(session());

		try {
			checkLoggedIn(state);

			RequestGetList request = Api.read(ctx(), RequestGetList.class);

			ResponseGetList response = new ResponseGetList();
			if (request.key.equals(INGREDIENTS_WORKING) ||
					request.key.equals(INGREDIENTS_NOT_WORKING) ||
					request.key.equals(INGREDIENTS_BAD_REACTION)) {
				response.results = new ArrayList<>();
				long[] ids = get_list(state.getUser(), request.key);
				for (long id : ids) {
					Ingredient ingredient = App.cache().ingredients.get(id);
					response.results.add(new IngredientController.ResponseIngredientObject(
							id,
							ingredient.getDisplayName(),
							ingredient.getCas_number(),
							ingredient.getDescription(),
							ingredient.getFunctionIds().toArray(),
							ingredient.getAliasesString(),
							ingredient.getProducts().size()
					));
				}
				return Api.write(response);
			} else {
				return Api.write(new ErrorResponse(Response.INVALID, "Get list key " + request.key + " is not valid."));
			}
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	public static boolean is_in_list(User user, String key, long id) {
		long[] ids = get_list(user, key);
		for (long ingredient_id : ids) {
			if (ingredient_id == id) {
				return true;
			}
		}
		return false;
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