package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ingredient extends Model {
	@Id
	private long id;

	@Column(length = 1024, unique = true)
	private String name;

	@Column(length = 128, unique = true)
	private String cas_number;

	@Column(length = 1024)
	private String short_desc;

	@OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER)
	private List<IngredientName> names = new ArrayList<>();

	@OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER)
	private List<IngredientFunction> functions = new ArrayList<>();

	public List<IngredientFunction> getFunctions() {
		return functions;
	}

	public List<IngredientName> getNames() {
		return names;
	}

	public String getShort_desc() {
		return short_desc;
	}

	public String getCas_number() {
		return cas_number;
	}

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

	//Static

	public static Finder<Long, Ingredient> find = new Finder<>(Long.class, Ingredient.class);

	public static Ingredient byId(long id) {
		return find.byId(id);
	}
}
