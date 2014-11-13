package src.models.userdata;

import src.models.user.User;
import src.models.util.BaseModel;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = Tracking.TABLENAME)
public class Tracking extends BaseModel {
	@Column
	private long timestamp;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Lob
	private String data;

	//Getters

	public long getTimestamp() {
		return timestamp;
	}

	public User getUser() {
		return user;
	}

	public String getData() {
		return data;
	}

	//Setters

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setData(String data) {
		this.data = data;
	}

	//Static

	public static final String TABLENAME = "tracking";

	public static Finder<Long, Tracking> find = new Finder<>(Long.class, Tracking.class);

	public static Tracking byId(long id) {
		return find.byId(id);
	}

	public static Tracking byUserAndDate(User user, long timestamp) {
		return find.where()
				.eq("user", user)
				.eq("timestamp", timestamp)
				.findUnique();
	}

	public static List<Tracking> byUserAndDateRange(User user, long timestamp_start, long timestamp_end) {
		return find.where()
				.eq("user", user)
				.between("timestamp", timestamp_start, timestamp_end)
				.findList();
	}
}
