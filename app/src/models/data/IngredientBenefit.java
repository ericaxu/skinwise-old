package src.models.data;

import src.App;
import src.models.util.BaseFinder;
import src.models.util.BaseModel;
import src.models.util.ManyToManyModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = IngredientBenefit.TABLENAME)
public class IngredientBenefit extends ManyToManyModel {

	//Relations

	public Ingredient getIngredient() {
		return App.cache().ingredients.get(getLeft_id());
	}

	public Benefit getBenefit() {
		return App.cache().benefits.get(getRight_id());
	}

	public void setIngredient(Ingredient ingredient) {
		setLeft_id(BaseModel.getIdIfExists(ingredient));
	}

	public void setBenefit(Benefit benefit) {
		setRight_id(BaseModel.getIdIfExists(benefit));
	}

	//Static

	public static final String TABLENAME = "ingredient_benefit";

	public static BaseFinder<IngredientBenefit> find = new BaseFinder<>(IngredientBenefit.class);
}
