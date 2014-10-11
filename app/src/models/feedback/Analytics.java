package src.models.feedback;

import src.models.BaseModel;
import src.models.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.List;

@Entity
public class Analytics extends BaseModel {
	@Column(length = 256)
	private String event;

	@Column(length = 1024)
	private String summary;

	@Lob
	private String data;

	@Column
	private long user_id;

	@Column
	private long timestamp;

	//Getters

	public String getEvent() {
		return event;
	}

	public String getSummary() {
		return summary;
	}

	public String getData() {
		return data;
	}

	public long getUser_id() {
		return user_id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	//Setters

	public void setEvent(String event) {
		this.event = event;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	//Others

	public User getUser() {
		return User.byId(user_id);
	}

	public void setUser(User user) {
		setUser_id(BaseModel.getIdIfExists(user));
	}

	public String getUserName() {
		User user = getUser();
		if (user == null) {
			return "";
		}
		return user.getName();
	}

	//Static

	public static Finder<Long, Analytics> find = new Finder<>(Long.class, Analytics.class);

	public static Analytics byId(long id) {
		return find.byId(id);
	}

	public static List<Analytics> byUser(long user_id, int page, int size) {
		return find.where().eq("user_id", user_id)
				.findPagingList(size)
				.getPage(page)
				.getList();
	}
}
