package src.models.userdata;

import src.models.user.User;
import src.models.util.BaseModel;

import javax.persistence.*;

@Entity
public class UserPreference extends BaseModel {
	@ManyToOne
	@JoinColumn(name = "user")
	private User user;

	@Column(name = "_key", length = 255)
	private String key;

	@Lob
	private String value;

	//Get

	public User getUser() {
		return user;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	//Set

	public void setUser(User user) {
		this.user = user;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
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
