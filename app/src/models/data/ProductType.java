package src.models.data;

import src.models.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class ProductType extends BaseModel {
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

	public static Finder<Long, ProductType> find = new Finder<>(Long.class, ProductType.class);

	public static ProductType byId(long id) {
		return find.byId(id);
	}

	public static ProductType byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}

	public static List<ProductType> all() {
		return find.all();
	}
}
