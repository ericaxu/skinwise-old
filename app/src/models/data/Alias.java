package src.models.data;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.util.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Alias.TABLENAME)
public class Alias extends NamedModel {

	private long ingredient_id;
	private transient LongHistory ingredient_id_tracker = new LongHistory();

	//Getters

	public long getIngredient_id() {
		return ingredient_id_tracker.getValue(ingredient_id);
	}

	//Setters

	public void setIngredient_id(long ingredient_id) {
		ingredient_id_tracker.setValue(this.ingredient_id, ingredient_id);
		this.ingredient_id = ingredient_id;
	}

	//Ingredient relation

	public Ingredient getIngredient() {
		return App.cache().ingredients.get(ingredient_id);
	}

	public void setIngredient(Ingredient ingredient) {
		long id = BaseModel.getIdIfExists(ingredient);
		setIngredient_id(id);
	}

	//Products relation

	public Set<ProductIngredient> getProductIngredient() {
		return App.cache().product_ingredient.getR(getId());
	}

	public TLongSet getProducts() {
		Set<ProductIngredient> result = getProductIngredient();
		return App.cache().product_ingredient.getIdsL(new TLongHashSet(), result);
	}

	//Others

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	@Override
	public void save() {
		super.save();
		ingredient_id_tracker.flush(App.cache().ingredient_alias, getId());
	}

	//Static
	public static final String TABLENAME = "alias";
	public static NamedFinder<Alias> find = new NamedFinder<>(Alias.class);

	public static List<Alias> unmatched(Page page) {
		List<Alias> result = page.apply(find.where().eq("ingredient_id", 0).query());
		return App.cache().alias.getList(App.cache().alias.getIds(result));
	}
}
