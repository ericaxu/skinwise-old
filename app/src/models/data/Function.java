package src.models.data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Function.TABLENAME)
public class Function extends NamedModel {

	//Relation table

	@ManyToMany(mappedBy = "functions", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	private Set<Ingredient> ingredients = new HashSet<>();

	//Getters

	public Set<Ingredient> getIngredients() {
		return ingredients;
	}

	//Setters

	//Static

	public static final String TABLENAME = "function";
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
