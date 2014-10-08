package models;

import play.db.ebean.Model;
import util.BCrypt;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User extends Model {
	@Id
	private long id;

	@Column(length = 512, unique = true)
	private String email;
	@Column(length = 256)
	private String password_hash;
	@Column(length = 256)
	private String password_salt;
	@Column(length = 512)
	private String name;

	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Permission> permissions = new HashSet<>();

	public User(String email, String password, String name) {
		this.email = email.toLowerCase();
		this.name = name;
		setPassword(password);
	}

	public void setPassword(String password) {
		this.password_salt = BCrypt.gensalt();
		this.password_hash = BCrypt.hashpw(password, password_salt);
	}

	public boolean checkPassword(String password) {
		return BCrypt.hashpw(password, password_salt).equals(password_hash);
	}

	public long getId() {
		return id;
	}

	public void addPermission(String permission) {
		permissions.add(Permission.get(permission));
	}

	public void removePermission(String permission) {
		permissions.remove(Permission.get(permission));
	}

	public boolean hasPermission(String permission) {
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
