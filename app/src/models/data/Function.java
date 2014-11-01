package src.models.data;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import src.App;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = Function.TABLENAME)
public class Function extends NamedModel {

	//Ingredients

	public Set<IngredientFunction> getIngredientFunction() {
		return App.cache().ingredient_function.getByR(getId());
	}

	public TLongSet getIngredients() {
		Set<IngredientFunction> result = getIngredientFunction();
		return App.cache().ingredient_function.getIdsL(new TLongHashSet(), result);
	}

	//Static
	public static final String TABLENAME = "function";
	public static NamedFinder<Function> find = new NamedFinder<>(Function.class);
}
