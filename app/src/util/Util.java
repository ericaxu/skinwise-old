package src.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class Util {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final double ML_IN_OZ = 29.5735;

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

	private static DecimalFormat priceFormatter = new DecimalFormat("#,###");

	public static long parsePrice(String price) throws NumberFormatException {
		if (price.isEmpty()) {
			return 0;
		}
		//Strip dollar sign
		if (price.startsWith("$")) {
			price = price.substring(1);
		}
		//Remove commas
		price = price.replace(",", "");

		String decimal = "0";
		//Has decimals
		if (price.contains(".")) {
			String[] pieces = price.split("\\.");
			price = pieces[0];
			decimal = pieces[1];
		}
		if (decimal.length() > 2) {
			throw new NumberFormatException(decimal + " is not a valid decimal for currency");
		}
		long result = Long.parseLong(price) * 100;
		result += Long.parseLong(decimal);
		return result;
	}

	public static String formatPrice(long price) {
		long whole = price / 100;
		long decimal = price - (whole * 100);
		String result = priceFormatter.format(whole);
		if (decimal != 0) {
			result += String.format(".%02d", decimal);
		}
		return "$" + result;
	}

	public static String formatPricePerOz(double price_per_ml) {
		DecimalFormat decimal_format = new DecimalFormat("0.##");
		double price_per_oz = price_per_ml * ML_IN_OZ / 100;
		return "$" + decimal_format.format(price_per_oz) + "/oz.";
	}

	public static double getNumberFrom(Matcher matcher, int group) {
		String result = StringUtils.strip(matcher.group(group), ".");
		try {
			return Double.parseDouble(result);
		}
		catch (NumberFormatException e) {
			Logger.debug("Util", result);
		}
		return 0;
	}

	public static String cleanTrim(String input) {
		return StringUtils.strip(input, "\t ,./?`~!@#$^&*;:=+-_\\|");
	}

	public static String stripAccents(String input) {
		return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	public static String formatNumber(double d) {
		if (d == (long) d) {
			return String.format("%d", (long) d);
		}
		else {
			return String.format("%s", d);
		}
	}

	public static String goodKey(String input) {
		input = stripAccents(input).replaceAll("[^0-9a-zA-Z ]", " ").trim().toLowerCase();
		return input.replaceAll("\\s+", " ");
	}

	public static String goodProductKey(String brand, String name) {
		return goodKey(brand + " " + name);
	}
}
