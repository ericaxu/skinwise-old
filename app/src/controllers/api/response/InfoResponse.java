package src.controllers.api.response;

public class InfoResponse extends Response {
	public InfoResponse(String info) {
		this.addMessage(ResponseMessage.info(info));
	}
}
