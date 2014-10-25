package src.models.data;

import src.App;
import src.models.util.BaseModel;
import src.models.util.Relation;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = ProductIngredient.TABLENAME)
public class ProductIngredient extends BaseModel implements Relation {
	private long product_id;
	private long alias_id;

	private int item_order;
	private boolean is_key;

	//Getters

	public Product getProduct() {
		return App.cache().products.get(product_id);
	}

	public Alias getAlias() {
		return App.cache().alias.get(alias_id);
	}

	public boolean isIs_key() {
		return is_key;
	}

	public int getItem_order() {
		return item_order;
	}

	@Override
	public long getLeftId() {
		return product_id;
	}

	@Override
	public long getRightId() {
		return alias_id;
	}
	//Setters

	public void setProduct(Product product) {
		this.product_id = BaseModel.getIdIfExists(product);
	}

	public void setAlias(Alias alias) {
		this.alias_id = BaseModel.getIdIfExists(alias);
	}

	public void setIs_key(boolean is_key) {
		this.is_key = is_key;
	}

	public void setItem_order(int item_order) {
		this.item_order = item_order;
	}

	//Static

	public static final String TABLENAME = "product_ingredient";

	public static Finder<Long, ProductIngredient> find = new Finder<>(Long.class, ProductIngredient.class);

	public static List<ProductIngredient> all() {
		return find.all();
	}

	public static List<ProductIngredient> byProductId(long product_id) {
		return find.where().eq("product_id", product_id).findList();
	}

	public static List<ProductIngredient> byAliasId(long alias_id) {
		return find.where().eq("alias_id", alias_id).findList();
	}

	public static final Sorter sorter = new Sorter();

	public static class Sorter implements Comparator<ProductIngredient> {
		@Override
		public int compare(ProductIngredient a, ProductIngredient b) {
			return Integer.compare(a.getItem_order(), b.getItem_order());
		}
	}
}
