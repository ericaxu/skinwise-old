package src.models.feedback;

import src.models.BaseModel;
import src.models.Page;
import src.models.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.List;

@Entity
public class Report extends BaseModel {

	@Column(length = 1024)
	private String path;

	@Column(length = 256)
	private String type;

	@Column(length = 1024)
	private String title;

	@Lob
	private String content;

	@Column
	private long user_id;

	@Column
	private boolean resolved;

	@Column
	private long timestamp;

	//Getters

	public String getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public long getUser_id() {
		return user_id;
	}

	public boolean isResolved() {
		return resolved;
	}

	public long getTimestamp() {
		return timestamp;
	}

	//Setters

	public void setPath(String path) {
		this.path = path;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
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

	public static Finder<Long, Report> find = new Finder<>(Long.class, Report.class);

	public static Report byId(long id) {
		return find.byId(id);
	}

	public static List<Report> all(Page page) {
		return page.apply(find.where().eq("resolved", false).query());
	}
}
