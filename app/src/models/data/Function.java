package src.models.data;

import src.models.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class Function extends BaseModel {

	@Column(length = 256)
	private String name;

	@Column(length = 1024)
	private String description;

	//Getters

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	//Setters

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	//Static

	public static Finder<Long, Function> find = new Finder<>(Long.class, Function.class);

	public static List<Function> all() {
		return find.all();
	}

	public static Function byId(long id) {
		return find.byId(id);
	}

	public static Function byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}
}
