import play.*;
import play.mvc.*;
import static play.mvc.Results.*;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		Logger.info("Application has started");
	}  

	@Override
	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}  

	//@Override
	//public Result onError(Throwable t) {
	//	return internalServerError(views.html.errorPage.render(t));
	//}  

	//@Override
	public Result onHandlerNotFound(String uri) {
		//super.onHandlerNotFound(uri);
		return notFound(views.html.pageNotFound.render(uri));
	}  

	//@Override
	public Result onBadRequest(String uri, String error) {
		//super.onBadRequest(uri, error);
		return badRequest("Don't try to hack the URI!");
	}  

}
