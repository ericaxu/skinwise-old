package src.models.userdata;

import src.models.util.BaseModel;
import src.models.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = Routine.TABLENAME)
public class Routine extends BaseModel {
	@Column(length = 255)
	private String name;

	@Column
	private boolean is_public;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	//Relation table

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, mappedBy = "routine")
	private List<RoutineItem> items = new ArrayList<>();

	//Getters

	public String getName() {
		return name;
	}

	public boolean isIs_public() {
		return is_public;
	}

	public User getUser() {
		return user;
	}

	public List<RoutineItem> getItems() {
		return items;
	}

	//Setters

	public void setName(String name) {
		this.name = name;
	}

	public void setIs_public(boolean is_public) {
		this.is_public = is_public;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setItems(List<RoutineItem> items) {
		this.items = items;
	}

	//Static

	public static final String TABLENAME = "routine";

	public static Finder<Long, Routine> find = new Finder<>(Long.class, Routine.class);

	public static Routine byId(long id) {
		return find.byId(id);
	}

	public static List<Routine> byUser(User user) {
		return find.where()
				.eq("user", user)
				.findList();
	}
}
