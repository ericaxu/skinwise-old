package src.models.data;

import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.util.BaseModel;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;
import src.models.util.Page;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Alias.TABLENAME)
public class Alias extends NamedModel {

	private long ingredient_id;
	private transient long ingredient_id_old = 0;
	private transient boolean ingredient_id_changed = false;

	//Getters

	public long getIngredient_id() {
		//Changed, let's still use the old value until the new one is flushed to DB.
		if (ingredient_id_changed) {
			return ingredient_id_old;
		}
		return ingredient_id;
	}

	//Setters

	public void setIngredient_id(long ingredient_id) {
		if (ingredient_id == this.ingredient_id) {
			//Nothing changed, switch back to unchanged if necessary
			ingredient_id_changed = false;
		}
		else {
			//Changed, keep a copy of old value
			if (!ingredient_id_changed) {
				ingredient_id_old = this.ingredient_id;
			}
			this.ingredient_id = ingredient_id;
			ingredient_id_changed = true;
		}
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

	public Set<ProductIngredient> getPairs() {
		return App.cache().product_ingredient.getR(this.getId());
	}

	//Others

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	@Override
	public void save() {
		if (ingredient_id_changed) {
			App.cache().ingredient_alias.remove(getId(), ingredient_id_old);
		}

		super.save();

		if (ingredient_id_changed) {
			App.cache().ingredient_alias.add(getId(), ingredient_id);
			ingredient_id_changed = false;
		}
	}

	//Static
	public static final String TABLENAME = "alias";
	public static NamedFinder<Alias> find = new NamedFinder<>(Alias.class);

	public static List<Alias> unmatched(Page page) {
		List<Alias> result = page.apply(find.where().eq("ingredient_id", 0).query());
		return App.cache().alias.getList(App.cache().alias.getIds(result));
	}
}
