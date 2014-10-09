package src.models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class IngredientFunction extends Model {
	@Id
	private long id;

	@Column(length = 256, unique = true)
	private String name;

	@Column(length = 1024)
	private String description;

	public IngredientFunction(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	//Static

	public static Finder<Long, IngredientFunction> find = new Finder<>(Long.class, IngredientFunction.class);

	public static IngredientFunction byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}
}
