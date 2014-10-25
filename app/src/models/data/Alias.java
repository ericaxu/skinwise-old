package src.models.data;

import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.util.BaseModel;
import src.models.Page;
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

	//Cached
	private transient List<ProductIngredient> pairs;
	private transient Set<Product> products;

	//Getters

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	public Ingredient getIngredient() {
		return App.cache().ingredients.get(ingredient_id);
	}

	//Setters

	public void setIngredient(Ingredient ingredient) {
		long new_id = BaseModel.getIdIfExists(ingredient);
		if (new_id != ingredient_id) {
			if (!BaseModel.isIdNull(ingredient_id)) {
				// Remove old ingredient from mapping in cache.
				App.cache().getNamesForIngredient(ingredient_id).remove(this);
			}
			if (!BaseModel.isIdNull(new_id)) {
				App.cache().getNamesForIngredient(new_id).add(this);
			}
		}
		this.ingredient_id = new_id;
	}

	//Cached getter/setters

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

	public static Set<Alias> byIngredientId(long ingredient_id) {
		List<Alias> result = find.where()
				.eq("ingredient_id", ingredient_id)
				.findList();
		Set<Alias> results = new HashSet<>();
		for (Alias alias : result) {
			results.add(App.cache().alias.get(alias.getId()));
		}
		return results;
	}

	public static List<Alias> unmatched(Page page) {
		return page.apply(find.where().eq("ingredient_id", 0).query());
	}
}
