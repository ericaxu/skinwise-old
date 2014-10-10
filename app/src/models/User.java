package src.models;

import src.util.BCrypt;

import javax.persistence.*;

@Entity
public class User extends Permissible {

	@Column(length = 512, unique = true)
	private String email;

	@Column(length = 256)
	private String password_hash;

	@Column(length = 512)
	private String name;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "group_id", referencedColumnName = "id")
	private Usergroup group;

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

	public void setEmail(String email) {
		this.email = email;
	}

	public void setName(String name) {
		this.name = name;
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

	@Override
	protected Permissible getParent() {
		return getGroup();
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
