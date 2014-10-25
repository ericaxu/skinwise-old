package src.models.data;

import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.BaseModel;
import src.models.Page;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = IngredientName.TABLENAME)
public class IngredientName extends NamedModel {

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
			pairs = ProductIngredient.byIngredientNameId(this.getId());
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

	public void saveProducts(Set<Product> newProducts) {
		List<ProductIngredient> oldPairs = getPairs();
		for (ProductIngredient oldPair : oldPairs) {
			oldPair.delete();
		}
		pairs.clear();
		products = new HashSet<>();
		for (Product newProduct : newProducts) {
			products.add(newProduct);
			ProductIngredient pair = new ProductIngredient();
			pair.setIngredient_name(this);
			pair.setProduct(newProduct);
			pair.save();
			pairs.add(pair);
		}
	}

	//Static

	public static final String TABLENAME = "ingredient_name";

	public static Finder<Long, IngredientName> find = new Finder<>(Long.class, IngredientName.class);

	public static List<IngredientName> all() {
		return find.all();
	}

	public static IngredientName byId(long id) {
		return find.byId(id);
	}

	public static IngredientName byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}

	public static Set<IngredientName> byIngredientId(long ingredient_id) {
		List<IngredientName> result = find.where()
				.eq("ingredient_id", ingredient_id)
				.findList();
		Set<IngredientName> results = new HashSet<>();
		for (IngredientName ingredientName : result) {
			results.add(App.cache().ingredient_names.get(ingredientName.getId()));
		}
		return results;
	}

	public static List<IngredientName> unmatched(Page page) {
		return page.apply(find.where().eq("ingredient_id", 0).query());
	}
}
