package src;

import com.typesafe.config.ConfigFactory;
import src.models.MemCache;
import src.models.user.Permissible;
import src.models.user.User;
import src.models.user.Usergroup;
import src.util.Backup;
import src.util.Logger;

public class App {
	private static final String TAG = "App";
	public static final String ADMIN_USER_GROUP = "Administrators";

	private static MemCache cache;
	private static Backup backup;
	private static Mode mode;

	public static void start() {
		stop();
		Usergroup admin_group = Usergroup.byName(ADMIN_USER_GROUP);
		if (admin_group == null) {
			admin_group = new Usergroup();
			admin_group.setName(ADMIN_USER_GROUP);
			admin_group.addPermission(Permissible.ALL);
			admin_group.save();
		}

		String admin_email = "admin@skinwise.com";
		String admin_pass = "test@123?!";
		User admin = User.byEmail(admin_email);
		if (admin == null) {
			admin = new User(admin_email, admin_pass, "Admin");
			admin.setGroup(admin_group);
			admin.save();
		}

		cache();
		getMode();
		backup = new Backup();
		backup.start();
		System.gc();
	}

	public static void stop() {
		cache = null;
		mode = null;
		if (backup != null) {
			backup.kill();
		}
		backup = null;
		System.gc();
	}

	public static MemCache cache() {
		if (cache == null) {
			synchronized (App.class) {
				if (cache == null) {
					Logger.info(TAG, "Memcache Loading..");
					cache = new MemCache();
					cache.init();
					Logger.info(TAG, "Memcache Loaded");
				}
			}
		}
		return cache;
	}

	public static Mode getMode() {
		if (mode == null) {
			mode = Mode.getMode(ConfigFactory.load().getString("application.mode"));
			Logger.info(TAG, "Run mode: " + mode.getName());
		}
		return mode;
	}

	public static boolean isDev() {
		return getMode() == Mode.Dev;
	}

	public static enum Mode {
		Dev("dev"),
		Test("test"),
		Prod("prod");
		private String name;

		private Mode(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static Mode getMode(String name) {
			for (Mode mode : Mode.values()) {
				if (mode.getName().equalsIgnoreCase(name)) {
					return mode;
				}
			}
			return Mode.Dev;
		}
	}
}
