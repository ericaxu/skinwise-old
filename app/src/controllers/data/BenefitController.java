package src.controllers.data;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.controllers.api.Api;
import src.controllers.util.Prettyfy;
import src.controllers.util.ResponseState;
import src.models.data.Benefit;
import views.html.benefit;

public class BenefitController extends Controller {
	public static class BenefitObject extends Api.ResponseNamedModelObject {
		public int ingredient_count;

		public BenefitObject(long id, String name, String description, int ingredient_count) {
			super(id, name, description);
			this.ingredient_count = ingredient_count;
		}
	}

	public static Result benefit(long id) {
		ResponseState state = new ResponseState(session());

		Benefit result = App.cache().benefits.get(id);
		if (result == null) {
			return ErrorController.notfound();
		}

		return ok(Prettyfy.prettify(benefit.render(state, result)));
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_benefit_byid() {
		return NamedDataController.api_named_model_byid(ctx(), App.cache().benefits, "Benefit", serializer);
	}

	@BodyParser.Of(BodyParser.TolerantText.class)
	public static Result api_benefit_all() {
		return NamedDataController.api_named_model_all(App.cache().benefits, serializer);
	}

	private static final BenefitSerializer serializer = new BenefitSerializer();

	public static class BenefitSerializer extends NamedDataController.Serializer<Benefit> {
		@Override
		public Api.ResponseNamedModelObject create(Benefit object) {
			return new BenefitObject(
					object.getId(),
					object.getName(),
					object.getDescription(),
					object.getIngredients().size()
			);
		}
	}
}
