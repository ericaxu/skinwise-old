package src.models.data;

import src.App;
import src.models.util.BaseFinder;
import src.models.util.BaseModel;
import src.models.util.ManyToManyModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Comparator;

@Entity
@Table(name = ProductIngredient.TABLENAME)
public class ProductIngredient extends ManyToManyModel {
	private int item_order;
	private boolean is_key;

	//Get/Set

	public boolean isIs_key() {
		return is_key;
	}

	public int getItem_order() {
		return item_order;
	}

	public void setIs_key(boolean is_key) {
		this.is_key = is_key;
	}

	public void setItem_order(int item_order) {
		this.item_order = item_order;
	}

	//Relations

	public Product getProduct() {
		return App.cache().products.get(getLeft_id());
	}

	public Alias getAlias() {
		return App.cache().alias.get(getRight_id());
	}

	public void setProduct(Product product) {
		setLeft_id(BaseModel.getIdIfExists(product));
	}

	public void setAlias(Alias alias) {
		setRight_id(BaseModel.getIdIfExists(alias));
	}

	//Static

	public static final String TABLENAME = "product_ingredient";

	public static BaseFinder<ProductIngredient> find = new BaseFinder<>(ProductIngredient.class);

	public static final Sorter sorter = new Sorter();

	public static class Sorter implements Comparator<ProductIngredient> {
		@Override
		public int compare(ProductIngredient a, ProductIngredient b) {
			return Integer.compare(a.getItem_order(), b.getItem_order());
		}
	}
}
