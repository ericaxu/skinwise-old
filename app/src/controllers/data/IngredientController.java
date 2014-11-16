package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.api.request.BadRequestException;
import src.controllers.api.response.ErrorResponse;
import src.controllers.util.Prettyfy;
import src.controllers.util.ResponseState;
import src.models.data.Alias;
import src.models.data.Function;
import src.models.data.Ingredient;
import src.models.util.Page;
import views.html.ingredient;

import java.util.List;

public class IngredientController extends Controller {
	public static class RequestIngredientFilter extends Api.RequestGetAllByPage {
		public long[] functions;
		public long[] benefits;

		public void sanitize() {
			functions = sanitize(functions);
			benefits = sanitize(benefits);
		}
	}

	public static class ResponseIngredientObject {
		public long id;
		public String name;
		public String display_name;
		public String cas_number;
		public String description;
		public long[] functions;
		public List<String> aliases;
		public int product_count;

		public ResponseIngredientObject(long id, String name, String display_name,
		                                String cas_number, String description,
		                                long[] functions, List<String> aliases,
		                                int product_count) {
			this.id = id;
			this.name = name;
			this.display_name = display_name;
			this.cas_number = cas_number;
			this.description = description;
			this.functions = functions;
			this.aliases = aliases;
			this.product_count = product_count;
		}
	}

	public static class ResponseAliasInfo extends Api.ResponseNamedModelObject {
		public ResponseIngredientObject ingredient;

		public ResponseAliasInfo(long id, String name, String description, ResponseIngredientObject ingredient) {
			super(id, name, description);
			this.ingredient = ingredient;
		}
	}

	public static Result ingredient(long id) {
		ResponseState state = new ResponseState(session());

		Ingredient result = App.cache().ingredients.get(id);
		if (result == null) {
			return ErrorController.notfound();
		}

		result.incrementPopularity(1);

		return ok(Prettyfy.prettify(ingredient.render(state, result)));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_ingredient_byid() {
		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Ingredient result = App.cache().ingredients.get(request.id);
			Api.checkNotNull(result, "Ingredient", request.id);

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = 1;

			response.results.add(new ResponseIngredientObject(
					result.getId(),
					result.getName(),
					result.getDisplay_name(),
					result.getCas_number(),
					result.getDescription(),
					result.getFunctionIds().toArray(),
					result.getAliasesString(),
					result.getProducts().size()
			));

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
			request.sanitize();

			for (long id : request.functions) {
				Function object = App.cache().functions.get(id);
				Api.checkNotNull(object, "Ingredient", id);
			}

			Page page = new Page(request.page, 20);
			List<Ingredient> result = Ingredient.byFilter(request.functions, request.benefits, page);

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = page.count;

			for (Ingredient ingredient : result) {
				response.results.add(new ResponseIngredientObject(
						ingredient.getId(),
						ingredient.getName(),
						ingredient.getDisplay_name(),
						ingredient.getCas_number(),
						ingredient.getDescription(),
						ingredient.getFunctionIds().toArray(),
						ingredient.getAliasesString(),
						ingredient.getProducts().size()
				));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_alias_byid() {
		try {
			Api.RequestGetById request = Api.read(ctx(), Api.RequestGetById.class);

			Alias result = App.cache().alias.get(request.id);
			Api.checkNotNull(result, "Ingredient", request.id);

			Ingredient ingredient = result.getIngredient();
			ResponseIngredientObject ingredientObject = null;
			if (ingredient != null) {
				ingredientObject = new ResponseIngredientObject(
						ingredient.getId(),
						ingredient.getName(),
						ingredient.getDisplay_name(),
						ingredient.getCas_number(),
						ingredient.getDescription(),
						ingredient.getFunctionIds().toArray(),
						ingredient.getAliasesString(),
						ingredient.getProducts().size()
				);
			}

			Api.ResponseResultList response = new Api.ResponseResultList();
			response.count = 1;

			response.results.add(new ResponseAliasInfo(result.getId(),
					result.getName(), result.getDescription(), ingredientObject
			));

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_alias_unmatched() {
		ResponseState state = new ResponseState(session());

		try {
			Api.RequestGetAllByPage request = Api.read(ctx(), Api.RequestGetAllByPage.class);

			List<Alias> result = Alias.unmatched(new Page(request.page));

			Api.ResponseResultList response = new Api.ResponseResultList();

			for (Alias object : result) {
				response.results.add(new Api.ResponseNamedModelObject(
						object.getId(),
						object.getName(),
						object.getDescription()
				));
			}

			return Api.write(response);
		}
		catch (BadRequestException e) {
			return Api.write(new ErrorResponse(e));
		}
	}
}