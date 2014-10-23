package src.models.data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Function.TABLENAME)
public class Function extends NamedModel {

	//Getters

	public Set<Ingredient> getIngredients() {
		// TODO: Relation
		return null;
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
