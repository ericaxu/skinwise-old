package src.util.dbimport;

import src.util.Json;

public class Import {
	public static class ImportResult {
		public String valid;
		public String invalid;
		public String failedReasons;

		public ImportResult(Object valid, Object invalid, Object failedReasons) {
			this.valid = Json.serialize(valid);
			this.invalid = Json.serialize(invalid);
			this.failedReasons = Json.serialize(failedReasons);
		}
	}
}
