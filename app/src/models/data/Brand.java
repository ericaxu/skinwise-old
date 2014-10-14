package src.models.data;

import src.models.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class Brand extends BaseModel {
	@Column(length = 1024, unique = true, nullable = false)
	private String name;

	@Column(length = 8192)
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

	public static Finder<Long, Brand> find = new Finder<>(Long.class, Brand.class);

	public static Brand byId(long id) {
		return find.byId(id);
	}

	public static Brand byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}

	public static List<Brand> all() {
		return find.all();
	}
}
