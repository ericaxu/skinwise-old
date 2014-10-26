package src;

import src.models.MemCache;
import src.models.user.Permissible;
import src.models.user.User;
import src.models.user.Usergroup;
import src.util.Backup;

public class App {
	public static final String ADMIN_USER_GROUP = "Administrators";

	private static MemCache cache;
	private static Backup backup;

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

		backup = new Backup();
		backup.start();
		cache();
	}

	public static void stop() {
		cache = null;
		if (backup != null) {
			backup.kill();
		}
		backup = null;
		System.gc();
	}

	public static MemCache cache() {
		if (cache == null) {
			cache = new MemCache();
			cache.init();
		}
		return cache;
	}
}
