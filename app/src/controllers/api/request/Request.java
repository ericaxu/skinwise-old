package src.controllers.api.request;

public abstract class Request {
	public long[] sanitize(long[] input) {
		if (input == null) {
			input = new long[0];
		}
		return input;
	}
}
