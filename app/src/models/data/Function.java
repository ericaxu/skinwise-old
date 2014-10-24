package src.models.data;

import src.App;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Function.TABLENAME)
public class Function extends NamedModel {

	//Cached
	private transient List<IngredientFunction> pairs;
	private transient Set<Ingredient> ingredients;

	//Getters

	//Setters

	//Cached getter/setters

	private List<IngredientFunction> getPairs() {
		if (pairs == null) {
			pairs = IngredientFunction.byFunctionId(this.getId());
		}
		return pairs;
	}

	public Set<Ingredient> getIngredients() {
		if (ingredients == null) {
			List<IngredientFunction> pairs = getPairs();
			ingredients = new HashSet<>();
			for (IngredientFunction pair : pairs) {
				ingredients.add(pair.getIngredient());
			}
		}
		return ingredients;
	}

	public void saveIngredients(Set<Ingredient> newIngredients) {
		List<IngredientFunction> oldPairs = getPairs();
		for (IngredientFunction oldPair : oldPairs) {
			oldPair.delete();
		}
		pairs.clear();
		ingredients = new HashSet<>();
		for (Ingredient newIngredient : newIngredients) {
			ingredients.add(newIngredient);
			IngredientFunction pair = new IngredientFunction();
			pair.setFunction(this);
			pair.setIngredient(newIngredient);
			pair.save();
			pairs.add(pair);
		}
	}

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
