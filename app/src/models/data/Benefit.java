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
@Table(name = Benefit.TABLENAME)
public class Benefit extends NamedModel {

	//Many-Many Ingredients relation

	public Set<IngredientBenefit> getIngredientBenefit() {
		return App.cache().ingredient_benefit.getByR(getId());
	}

	public TLongSet getIngredients() {
		Set<IngredientBenefit> result = getIngredientBenefit();
		return App.cache().ingredient_benefit.getIdsL(new TLongHashSet(), result);
	}

	//Static

	public static final String TABLENAME = "benefit";

	public static NamedFinder<Benefit> find = new NamedFinder<>(Benefit.class);
}
