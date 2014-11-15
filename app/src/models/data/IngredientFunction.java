package src.models.data;

import src.App;
import src.models.util.BaseFinder;
import src.models.util.BaseModel;
import src.models.util.ManyToManyModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = IngredientFunction.TABLENAME)
public class IngredientFunction extends ManyToManyModel {

	//Relations

	public Ingredient getIngredient() {
		return App.cache().ingredients.get(getLeft_id());
	}

	public Function getFunction() {
		return App.cache().functions.get(getRight_id());
	}

	public void setIngredient(Ingredient ingredient) {
		setLeft_id(BaseModel.getIdIfExists(ingredient));
	}

	public void setFunction(Function function) {
		setRight_id(BaseModel.getIdIfExists(function));
	}

	//Static

	public static final String TABLENAME = "ingredient_function";

	public static BaseFinder<IngredientFunction> find = new BaseFinder<>(IngredientFunction.class);
}
