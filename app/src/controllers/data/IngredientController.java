package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.request.NotNull;
import src.controllers.api.response.ErrorResponse;
import src.controllers.api.response.Response;
import src.controllers.util.ResponseState;
import src.models.Page;
import src.models.data.Function;
import src.models.data.Ingredient;
import views.html.ingredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IngredientController extends Controller {

	public static class ResponseIngredient extends Response {
		public long id;
		public String name;
		public String cas_number;
		public String description;
		public List<String> functions;
		public List<String> names;

		public ResponseIngredient(long id, String name,
		                          String cas_number, String description,
		                          List<String> functions, List<String> names) {
			this.id = id;
			this.name = name;
			this.cas_number = cas_number;
			this.description = description;
			this.functions = functions;
			this.names = names;
		}
	}

	public static class RequestIngredientFilter extends Api.RequestGetAllByPage {
		@NotNull
		public long[] functions;
	}

	public static class ResponseIngredientObject {
		public long id;
		public String name;
		public String cas_number;
		public String description;
		public List<Long> functions;
		public List<String> names;

		public ResponseIngredientObject(long id, String name,
		                                String cas_number, String description,
		                                List<Long> functions, List<String> names) {
			this.id = id;
			this.name = name;
			this.cas_number = cas_number;
			this.description = description;
			this.functions = functions;
			this.names = names;
		}
	}

	public static class ResponseIngredientFilter extends Response {
		public List<ResponseIngredientObject> results = new ArrayList<>();
		public int count;
	}

	public static class ResponseFunctionObject {
		public long id;
		public String name;
		public String description;

		public ResponseFunctionObject(long id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}
	}

	public static class ResponseFunctions extends Response {
		public List<ResponseFunctionObject> results = new ArrayList<>();
	}

	public static Result ingredient(long id) {
		ResponseState state = new ResponseState(session());

		Ingredient result = App.cache().ingredients.get(id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(ingredient.render(state, result));
	}

	public static Result ingredient_search(String query) {
		ResponseState state = new ResponseState(session());

		//TODO

		return ok();
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_ingredient_byid() {
		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Ingredient result = App.cache().ingredients.get(request.id);
			if (result == null) {
				throw new BadRequestException(Response.NOT_FOUND, "Ingredient not found");
			}

			Response response = new ResponseIngredient(
					result.getId(),
					result.getName(),
					result.getCas_number(),
					result.getDescription(),
					result.getFunctionsString(),
					result.getNamesString()
			);

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_ingredient_filter() {
		try {
			RequestIngredientFilter request = Api.read(ctx(), RequestIngredientFilter.class);

			for (long function_id : request.functions) {
				Function function = App.cache().functions.get(function_id);
				if (function == null) {
					throw new BadRequestException(Response.NOT_FOUND, "Function not found");
				}
			}

			Page page = new Page(request.page, 20);
			List<Ingredient> result = Ingredient.byFilter(request.functions, page);

			ResponseIngredientFilter response = new ResponseIngredientFilter();
			response.count = page.count;

			for (Ingredient ingredient : result) {
				response.results.add(new ResponseIngredientObject(
						ingredient.getId(),
						ingredient.getDisplayName(),
						ingredient.getCas_number(),
						ingredient.getDescription(),
						ingredient.getFunctionIds(),
						ingredient.getNamesString()
				));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_functions() {
		Collection<Function> result = App.cache().functions.all();

		ResponseFunctions response = new ResponseFunctions();

		for (Function function : result) {
			response.results.add(new ResponseFunctionObject(
					function.getId(),
					function.getName(),
					function.getDescription()
			));
		}

		return Api.write(response);
	}
}