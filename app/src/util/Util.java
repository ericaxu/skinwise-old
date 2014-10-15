package src.util;

import java.io.File;
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
		File f = new File(file);
		if (!f.exists()) {
			f.createNewFile();
		}
		Files.write(Paths.get(file), data.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static String joinString(String delimiter, long[] array) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < array.length - 1; i++) {
			result.append(array[i]).append(delimiter);
		}
		result.append(array[array.length - 1]);
		return result.toString();
	}

	public static <T> String joinString(String delimiter, T[] array) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < array.length - 1; i++) {
			result.append(array[i]).append(delimiter);
		}
		result.append(array[array.length - 1]);
		return result.toString();
	}
}
