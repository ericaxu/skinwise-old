package src.models.data;

import src.App;
import src.models.util.BaseFinder;
import src.models.util.BaseModel;
import src.models.util.ManyToMany;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = IngredientFunction.TABLENAME)
public class IngredientFunction extends BaseModel implements ManyToMany {
	private long ingredient_id;
	private long function_id;

	//Getters

	public long getIngredient_id() {
		return ingredient_id;
	}

	public long getFunction_id() {
		return function_id;
	}

	//Setters

	public void setIngredient_id(long ingredient_id) {
		this.ingredient_id = ingredient_id;
	}

	public void setFunction_id(long function_id) {
		this.function_id = function_id;
	}

	//Relations

	public Ingredient getIngredient() {
		return App.cache().ingredients.get(ingredient_id);
	}

	public Function getFunction() {
		return App.cache().functions.get(function_id);
	}

	public void setIngredient(Ingredient ingredient) {
		setIngredient_id(BaseModel.getIdIfExists(ingredient));
	}

	public void setFunction(Function function) {
		setFunction_id(BaseModel.getIdIfExists(function));
	}

	//ManyToMany relations

	@Override
	public long getLeftId() {
		return getIngredient_id();
	}

	@Override
	public long getRightId() {
		return getFunction_id();
	}

	//Static

	public static final String TABLENAME = "ingredient_function";

	private static BaseFinder<IngredientFunction> find = new BaseFinder<>(IngredientFunction.class);

	public static List<IngredientFunction> all() {
		return find.all();
	}

	public static List<IngredientFunction> byIngredientId(long ingredient_id) {
		if (BaseModel.isIdNull(ingredient_id)) {
			return new ArrayList<>();
		}
		return find.where().eq("ingredient_id", ingredient_id).findList();
	}

	public static List<IngredientFunction> byFunctionId(long function_id) {
		if (BaseModel.isIdNull(function_id)) {
			return new ArrayList<>();
		}
		return find.where().eq("function_id", function_id).findList();
	}
}
