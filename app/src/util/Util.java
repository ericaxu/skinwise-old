package src.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
	public static String notNull(String input) {
		if (input == null) {
			return "";
		}
		return input;
	}

	public static String readAll(String file) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(file));
		return new String(data);
	}
}
