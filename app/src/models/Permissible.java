package src.models;

import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
public abstract class Permissible extends BaseModel {
	@Lob
	private String permissions = "";
	private transient String permissions_cache = "";
	private transient Set<String> permissions_set = new HashSet<>();

	//Getters

	public String getPermissions() {
		return permissions;
	}

	//Setters

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	//Others

	public void savePermissions() {
		setPermissions(writePermissions(permissions_set));
		permissions_cache = permissions;
	}

	public void addPermission(String permission) {
		getPermissions_set().add(permission);
		savePermissions();
	}

	public void removePermission(String permission) {
		getPermissions_set().remove(permission);
		savePermissions();
	}

	public Set<String> getPermissions_set() {
		// permission set and string out of sync
		if (permissions_set == null || !permissions_cache.equals(permissions)) {
			permissions_set = readPermissions(permissions);
			permissions_cache = permissions;
		}
		return permissions_set;
	}

	public void setPermissions_set(Set<String> permissions) {
		this.permissions_set = permissions;
		savePermissions();
	}

	public boolean hasPermission(String permission) {
		Permissible parent = getParent();
		if (parent != null) {
			if (parent.hasPermission(permission)) {
				return true;
			}
		}

		return hasPermission(getPermissions_set(), permission);
	}

	protected abstract Permissible getParent();

	//Static

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
