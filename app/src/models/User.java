package src.models;

import play.db.ebean.Model;
import src.user.Permissible;
import src.util.BCrypt;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User extends Model implements Permissible {
	@Id
	private long id;

	@Column(length = 512, unique = true)
	private String email;
	@Column(length = 256)
	private String password_hash;
	@Column(length = 512)
	private String name;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "group_id", referencedColumnName = "id")
	private Usergroup group;

	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Permission> permissions = new HashSet<>();

	public User(String email, String password, String name) {
		this.email = email.toLowerCase();
		this.name = name;
		setPassword(password);
	}

	public void setPassword(String password) {
		this.password_hash = BCrypt.hashpw(password, BCrypt.gensalt());
	}

	public boolean checkPassword(String password) {
		return BCrypt.checkpw(password, password_hash);
	}

	public long getId() {
		return id;
	}

	public Usergroup getGroup() {
		return group;
	}

	public void setGroup(Usergroup group) {
		this.group = group;
	}

	public void addPermission(String permission) {
		permissions.add(Permission.get(permission));
	}

	public void removePermission(String permission) {
		permissions.remove(Permission.get(permission));
	}

	public boolean hasPermission(String permission) {
		if(getGroup().hasPermission(permission)) {
			return true;
		}
		return permissions.contains(Permission.get(permission));
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
