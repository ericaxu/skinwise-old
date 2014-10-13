package src.models.data;

import src.models.BaseModel;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class ProductIngredient extends BaseModel {
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	private Product product;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	private IngredientName ingredient_name;

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

	//Others

	public void setProduct(Product product) {
		this.product = product;
	}

	public void setIngredient_name(IngredientName ingredient_name) {
		this.ingredient_name = ingredient_name;
	}

	public void setIs_key(boolean is_key) {
		this.is_key = is_key;
	}
}
