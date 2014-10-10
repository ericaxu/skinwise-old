package src.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Permission {
	public static class ADMIN {
		public static final String ALL = "ADMIN.*";
		public static final String VIEW = "ADMIN.VIEW";
		public static final String IMPORT = "ADMIN.IMPORT";
		public static final String USER = "ADMIN.USER";
	}

	public static boolean hasPermission(Set<String> permissions, String permission) {
		String[] parts = permission.split("\\.");
		for (int i = 0; i < parts.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < i; j++) {
				sb.append(parts[j]).append(".");
			}
			sb.append("*");
			String perm = sb.toString();
			if (permissions.contains(perm)) {
				return true;
			}
		}
		return permissions.contains(permission);
	}

	public static String writePermissions(Set<String> permissions) {
		StringBuilder sb = new StringBuilder();
		for (String permission : permissions) {
			if (permission != null && !permission.isEmpty()) {
				sb.append(permission).append(",");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public static Set<String> readPermissions(String permissions) {
		Set<String> perms = new HashSet<>();
		perms.addAll(Arrays.asList(permissions.split(",")));
		return perms;
	}
}
