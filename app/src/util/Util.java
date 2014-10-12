package src.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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

	public static void writeAll(String file, String data) throws IOException {
		Files.write(Paths.get(file), data.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
	}
}
