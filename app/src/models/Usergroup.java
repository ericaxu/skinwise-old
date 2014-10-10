package src.models;

import src.user.Permission;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Usergroup extends BaseModel {

	@Column(length = 512, unique = true)
	private String name;

	public Usergroup(String name) {
		this.name = name;
	}

	@Lob
	private String permissions = "";
	private transient String permissions_cache = "";
	private transient Set<String> permissions_set = new HashSet<>();

	//Getters

	public String getName() {
		return name;
	}

	//Setters

	public void setName(String name) {
		this.name = name;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	//Others

	public Set<String> getPermissions_set() {
		// permission set and string out of sync
		if (permissions_set == null || !permissions_cache.equals(permissions)) {
			permissions_set = Permission.readPermissions(permissions);
			permissions_cache = permissions;
		}
		return permissions_set;
	}

	public void savePermissions() {
		setPermissions(Permission.writePermissions(permissions_set));
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

	public boolean hasPermission(String permission) {
		return Permission.hasPermission(getPermissions_set(), permission);
	}

	//Static

	private static Finder<Long, Usergroup> find = new Finder<>(Long.class, Usergroup.class);

	public static List<Usergroup> all() {
		return find.all();
	}

	public static Usergroup byId(long id) {
		return find.byId(id);
	}

	public static Usergroup byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}
}
