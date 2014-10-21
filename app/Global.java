import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import src.App;
import src.controllers.ErrorController;
import src.util.Logger;

public class Global extends GlobalSettings {
	private static final String TAG = "Global";

	@Override
	public void onStart(Application app) {
		App.start();
	}

	@Override
	public void onStop(Application app) {
		App.stop();
	}

	@Override
	public F.Promise<Result> onHandlerNotFound(Http.RequestHeader requestHeader) {
		return F.Promise.pure(ErrorController.notfound());
	}

	@Override
	public F.Promise<Result> onBadRequest(Http.RequestHeader requestHeader, String s) {
		return F.Promise.pure(ErrorController.notfound());
	}

	@Override
	public F.Promise<Result> onError(Http.RequestHeader requestHeader, Throwable e) {
		Logger.error(TAG, e);

		if (requestHeader.method().equalsIgnoreCase("POST") && requestHeader.path().startsWith("/api")) {
			return F.Promise.pure(ErrorController.api_error(e));
		}
		return F.Promise.pure(ErrorController.error(e));
	}
}