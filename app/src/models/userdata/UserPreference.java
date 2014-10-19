package src.models.userdata;

import play.db.ebean.Model;
import src.models.user.User;

import javax.persistence.*;

@Entity
public class UserPreference extends Model {
	@Id
	private long id;

	@ManyToOne
	@JoinColumn(name = "user")
	private User user;

	@Column(name = "_key", length = 255)
	private String key;

	@Lob
	private String value;

	public UserPreference(User user, String key, String value) {
		this.user = user;
		this.key = key;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	//Static

	private static Finder<Long, UserPreference> find = new Finder<>(Long.class, UserPreference.class);

	public static UserPreference byUserAndKey(User user, String key) {
		return find.where()
				.eq("user", user)
				.eq("key", key)
				.findUnique();
	}
}
