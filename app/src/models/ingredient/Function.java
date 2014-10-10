package src.models.ingredient;

import src.models.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;

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

	//Others

	public void loadFrom(Function other) {
		setName(other.getName());
		setDescription(other.getDescription());
	}

	//Static

	public static Finder<Long, Function> find = new Finder<>(Long.class, Function.class);

	public static Function byId(long id) {
		return find.byId(id);
	}

	public static Function byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}
}
