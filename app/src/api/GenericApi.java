package src.api;

import src.api.request.Request;

public class GenericApi {
	public static class RequestGetById extends Request {
		public long id;
	}

	public static class RequestGetByIdAll extends RequestGetById {
		public boolean all;
	}
}
