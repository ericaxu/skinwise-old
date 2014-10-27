package src.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class Util {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	public static String notNull(String input) {
		if (input == null) {
			return "";
		}
		return input;
	}

	public static String readAll(String file) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(file));
		return new String(data, UTF8);
	}

	public static void writeAll(String file, String data) throws IOException {
		File f = new File(file);
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		Files.write(Paths.get(file), data.getBytes(UTF8), StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static String joinString(String delimiter, long[] array) {
		if (array.length == 0) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < array.length - 1; i++) {
			result.append(array[i]).append(delimiter);
		}
		result.append(array[array.length - 1]);
		return result.toString();
	}

	public static <T> String joinString(String delimiter, T[] array) {
		return joinString(delimiter, Arrays.asList(array));
	}

	public static <T> String joinString(String delimiter, List<T> array) {
		if (array.size() == 0) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < array.size() - 1; i++) {
			result.append(array.get(i)).append(delimiter);
		}
		result.append(array.get(array.size() - 1));
		return result.toString();
	}

	public static String goodKey(String input) {
		input = input.replaceAll("[^0-9a-zA-Z ]", " ").trim().toLowerCase();
		return input.replaceAll("\\s+", " ");
	}

	public static String goodProductKey(String brand, String name) {
		return goodKey(brand + " " + name);
	}
}
