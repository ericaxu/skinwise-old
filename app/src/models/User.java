package src.models;

import src.user.Permissible;
import src.user.Permission;
import src.util.BCrypt;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User extends BaseModel implements Permissible {

	@Column(length = 512, unique = true)
	private String email;

	@Column(length = 256)
	private String password_hash;

	@Column(length = 512)
	private String name;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "group_id", referencedColumnName = "id")
	private Usergroup group;

	@Lob
	private String permissions = "";
	private transient String permissions_cache = "";
	private transient Set<String> permissions_set = new HashSet<>();

	public User(String email, String password, String name) {
		this.email = email.toLowerCase();
		this.name = name;
		setPassword(password);
	}

	//Getters

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public Usergroup getGroup() {
		return group;
	}

	//Setters

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public void setPassword(String password) {
		this.password_hash = BCrypt.hashpw(password, BCrypt.gensalt());
	}

	//Others

	public boolean checkPassword(String password) {
		return BCrypt.checkpw(password, password_hash);
	}

	public void setGroup(Usergroup group) {
		this.group = group;
	}

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
		if (getGroup().hasPermission(permission)) {
			return true;
		}
		return Permission.hasPermission(getPermissions_set(), permission);
	}

	//Static

	private static Finder<Long, User> find = new Finder<>(Long.class, User.class);

	public static User byId(long id) {
		return find.byId(id);
	}

	public static User byEmail(String email) {
		return find.where()
				.eq("email", email.toLowerCase())
				.findUnique();
	}
}
