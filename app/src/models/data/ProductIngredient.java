package src.models.data;

import src.models.BaseModel;

import javax.persistence.*;
import java.util.Comparator;

@Entity
@Table(name = ProductIngredient.TABLENAME)
public class ProductIngredient extends BaseModel {
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "ingredient_name_id", referencedColumnName = "id")
	private IngredientName ingredient_name;

	private int item_order;
	private boolean is_key;

	//Getters

	public Product getProduct() {
		return product;
	}

	public IngredientName getIngredient_name() {
		return ingredient_name;
	}

	public boolean isIs_key() {
		return is_key;
	}

	public int getItem_order() {
		return item_order;
	}

	//Setters

	public void setProduct(Product product) {
		this.product = product;
	}

	public void setIngredient_name(IngredientName ingredient_name) {
		this.ingredient_name = ingredient_name;
	}

	public void setIs_key(boolean is_key) {
		this.is_key = is_key;
	}

	public void setItem_order(int item_order) {
		this.item_order = item_order;
	}

	//Static

	public static final String TABLENAME = "product_ingredient";

	public static final Sorter sorter = new Sorter();

	public static class Sorter implements Comparator<ProductIngredient> {
		@Override
		public int compare(ProductIngredient a, ProductIngredient b) {
			return Integer.compare(a.getItem_order(), b.getItem_order());
		}
	}
}
