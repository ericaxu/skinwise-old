package src.models.data;

import src.App;
import src.models.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = IngredientFunction.TABLENAME)
public class IngredientFunction extends BaseModel {
	private long ingredient_id;
	private long function_id;

	//Getters

	public Ingredient getIngredient() {
		return App.cache().ingredients.get(ingredient_id);
	}

	public Function getFunction() {
		return App.cache().functions.get(function_id);
	}

	//Setters

	public void setIngredient(Ingredient ingredient) {
		this.ingredient_id = BaseModel.getIdIfExists(ingredient);
	}

	public void setFunction(Function function) {
		this.function_id = BaseModel.getIdIfExists(function);
	}

	//Static

	public static final String TABLENAME = "ingredient_function";
}
