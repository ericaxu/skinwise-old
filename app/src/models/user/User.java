package src.models.user;

import play.db.ebean.Model;
import src.models.Page;
import src.models.Permissible;
import src.util.BCrypt;

import javax.persistence.*;
import java.util.List;

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

	public String getGroupName() {
		Usergroup group = getGroup();
		if (group != null) {
			return group.getName();
		}
		return "";
	}

	//Setters

	public void setEmail(String email) {
		this.email = email;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setGroup(Usergroup group) {
		this.group = group;
	}

	public void setPassword(String password) {
		this.password_hash = BCrypt.hashpw(password, BCrypt.gensalt());
	}

	//Others

	public boolean checkPassword(String password) {
		return BCrypt.checkpw(password, password_hash);
	}

	@Override
	protected Permissible getParent() {
		return getGroup();
	}

	//Static

	private static Model.Finder<Long, User> find = new Model.Finder<>(Long.class, User.class);

	public static User byId(long id) {
		return find.byId(id);
	}

	public static User byEmail(String email) {
		return find.where()
				.eq("email", email.toLowerCase())
				.findUnique();
	}

	public static List<User> all(Page page) {
		return page.apply(find).findList();
	}
}
