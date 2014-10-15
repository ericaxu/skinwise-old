package src.util;

import play.db.DB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

	public static int sqlCount(String query_from) throws SQLException {
		Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result_set = statement.executeQuery("SELECT COUNT(*) " + query_from);
		result_set.next();
		int count = result_set.getInt("COUNT(*)");
		result_set.close();
		connection.close();
		return count;
	}
}
