package src;

import src.models.MemCache;

public class App {
	private static MemCache cache;

	public static MemCache cache() {
		if (cache == null) {
			cache = new MemCache();
			cache.init();
		}
		return cache;
	}
}
