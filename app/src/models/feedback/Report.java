package src.models.feedback;

import src.models.BaseModel;
import src.models.user.User;

import javax.persistence.*;
import java.util.List;

@Entity
public class Report extends BaseModel {

	@Column(length = 1024)
	private String path;

	@Column(length = 1024)
	private String title;

	@Lob
	private String content;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "reported_by", referencedColumnName = "id")
	private User reported_by;

	@Column
	private long timestamp;

	//Getters

	public String getPath() {
		return path;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public User getReported_by() {
		return reported_by;
	}

	public long getTimestamp() {
		return timestamp;
	}

	//Setters

	public void setPath(String path) {
		this.path = path;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setReported_by(User reported_by) {
		this.reported_by = reported_by;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	//Others

	//Static

	public static Finder<Long, Report> find = new Finder<>(Long.class, Report.class);

	public static Report byId(long id) {
		return find.byId(id);
	}

	public static List<Report> all(int page, int size) {
		return find.findPagingList(size)
				.getPage(page)
				.getList();
	}
}
