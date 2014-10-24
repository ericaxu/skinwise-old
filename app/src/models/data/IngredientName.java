package src.models.data;

import org.apache.commons.lang3.text.WordUtils;
import src.models.Page;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = IngredientName.TABLENAME)
public class IngredientName extends NamedModel {

	//Cached
	private transient List<ProductIngredient> pairs;
	private transient Set<Product> products;

	//Non-columns
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ingredient_id", referencedColumnName = "id")
	private Ingredient ingredient;

	//Getters

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	//Setters

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
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

	public static List<IngredientName> unmatched(Page page) {
		return page.apply(find.where().isNull("ingredient").query());
	}
}
