package src.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {
	public static void fatal(String tag, String message) {
		fatal(tag, message, new Exception(message));
	}

	public static <T extends Throwable> T fatal(String tag, String message, T e) {
		error(tag, e);
		return e;
	}

	//Non-logging calls

	public static void error(String tag, String message) {
		error(tag, new Exception(message));
	}

	public static <T extends Throwable> T error(String tag, T e) {
		play.Logger.error("[" + tag + "] " + e.getMessage() + " " + getStackTrace(e));
		return e;
	}

	public static <T extends Throwable> T expect(String tag, T e) {
		play.Logger.debug("[" + tag + "] " + e.getMessage() + " " + getStackTrace(e));
		return e;
	}

	public static void info(String tag, String message) {
		play.Logger.info("[" + tag + "] " + message);
	}

	public static void debug(String tag, String message) {
		play.Logger.debug("[" + tag + "] " + message);
	}

	public static String getHtmlStackTrace(Throwable t) {
		return getStackTrace(t).replace("\n", "\n<br>");
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}