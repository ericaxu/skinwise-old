package src.models.data;

import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.Page;
import src.models.util.BaseModel;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Alias.TABLENAME)
public class Alias extends NamedModel {

	private long ingredient_id;

	//Get/Set

	public long getIngredient_id() {
		return ingredient_id;
	}

	public void setIngredient_id(long ingredient_id) {
		if (ingredient_id != this.ingredient_id) {
			App.cache().ingredient_aliases.remove(this);
			this.ingredient_id = ingredient_id;
			App.cache().ingredient_aliases.add(this);
		}
	}

	//Relations

	public Ingredient getIngredient() {
		return App.cache().ingredients.get(ingredient_id);
	}

	public void setIngredient(Ingredient ingredient) {
		long id = BaseModel.getIdIfExists(ingredient);
		setIngredient_id(id);
	}

	//Others

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	//Cached getter/setters

	private transient List<ProductIngredient> pairs;
	private transient Set<Product> products;

	private List<ProductIngredient> getPairs() {
		if (pairs == null) {
			pairs = ProductIngredient.byAliasId(this.getId());
		}
		return pairs;
	}

	public Set<Product> getProducts() {
		if (products == null) {
			List<ProductIngredient> pairs = getPairs();
			products = new HashSet<>();
			for (ProductIngredient pair : pairs) {
				products.add(pair.getProduct());
			}
		}
		return products;
	}

	//Static
	public static final String TABLENAME = "alias";
	public static NamedFinder<Alias> find = new NamedFinder<>(Alias.class);

	public static List<Alias> unmatched(Page page) {
		return page.apply(find.where().eq("ingredient_id", 0).query());
	}
}
