package src.models.data;

import src.App;
import src.models.util.BaseFinder;
import src.models.util.BaseModel;
import src.models.util.ManyToMany;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = ProductIngredient.TABLENAME)
public class ProductIngredient extends BaseModel implements ManyToMany {
	private long product_id;
	private long alias_id;

	private int item_order;
	private boolean is_key;

	//Getters

	public long getProduct_id() {
		return product_id;
	}

	public long getAlias_id() {
		return alias_id;
	}

	public boolean isIs_key() {
		return is_key;
	}

	public int getItem_order() {
		return item_order;
	}

	//Setters

	public void setProduct_id(long product_id) {
		this.product_id = product_id;
	}

	public void setAlias_id(long alias_id) {
		this.alias_id = alias_id;
	}

	public void setIs_key(boolean is_key) {
		this.is_key = is_key;
	}

	public void setItem_order(int item_order) {
		this.item_order = item_order;
	}

	//Relations

	public Product getProduct() {
		return App.cache().products.get(product_id);
	}

	public Alias getAlias() {
		return App.cache().alias.get(alias_id);
	}

	public void setProduct(Product product) {
		setProduct_id(BaseModel.getIdIfExists(product));
	}

	public void setAlias(Alias alias) {
		setAlias_id(BaseModel.getIdIfExists(alias));
	}

	//ManyToMany relations

	@Override
	public long getLeftId() {
		return getProduct_id();
	}

	@Override
	public long getRightId() {
		return getAlias_id();
	}

	//Static

	public static final String TABLENAME = "product_ingredient";

	public static BaseFinder<ProductIngredient> find = new BaseFinder<>(ProductIngredient.class);

	public static List<ProductIngredient> all() {
		return find.all();
	}

	public static final Sorter sorter = new Sorter();

	public static class Sorter implements Comparator<ProductIngredient> {
		@Override
		public int compare(ProductIngredient a, ProductIngredient b) {
			return Integer.compare(a.getItem_order(), b.getItem_order());
		}
	}
}
