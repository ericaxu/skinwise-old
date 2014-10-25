package src.models.data;

import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.Page;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;
import src.util.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Ingredient.TABLENAME)
public class Ingredient extends NamedModel {

	private long popularity;

	@Column(length = 127)
	private String cas_number;

	//Getters

	public long getPopularity() {
		return popularity;
	}

	public String getCas_number() {
		return cas_number;
	}

	//Setters

	public void setPopularity(long popularity) {
		this.popularity = popularity;
	}

	public void setCas_number(String cas_number) {
		this.cas_number = cas_number;
	}

	//Functions

	private transient List<IngredientFunction> ingredient_functions;
	private transient Set<Function> functions;

	private List<IngredientFunction> getIngredient_functions() {
		if (ingredient_functions == null) {
			ingredient_functions = IngredientFunction.byIngredientId(this.getId());
		}
		return ingredient_functions;
	}

	public Set<Function> getFunctions() {
		if (functions == null) {
			getIngredient_functions();
			functions = new HashSet<>();
			for (IngredientFunction ingredient_function : ingredient_functions) {
				functions.add(ingredient_function.getFunction());
			}
		}
		return functions;
	}

	public void saveFunctions(Set<Function> input) {
		getIngredient_functions();
		for (IngredientFunction ingredient_function : ingredient_functions) {
			ingredient_function.delete();
		}
		ingredient_functions.clear();
		for (Function function : input) {
			IngredientFunction ingredient_function = new IngredientFunction();
			ingredient_function.setFunction(function);
			ingredient_function.setIngredient(this);
			ingredient_function.save();
			ingredient_functions.add(ingredient_function);
		}
		functions = input;
	}

	public List<String> getFunctionsString() {
		List<String> result = new ArrayList<>();
		for (Function function : getFunctions()) {
			result.add(WordUtils.capitalizeFully(function.getName()));
		}
		return result;
	}

	public List<Long> getFunctionIds() {
		List<Long> result = new ArrayList<>();
		for (Function function : getFunctions()) {
			result.add(function.getId());
		}
		return result;
	}

	//Names

	public Set<Alias> getNames() {
		return App.cache().getNamesForIngredient(this.getId());
	}

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	public List<String> getNamesString() {
		List<String> result = new ArrayList<>();
		for (Alias name : this.getNames()) {
			result.add(name.getName());
		}
		return result;
	}

	//Static

	public static final String TABLENAME = "ingredient";

	public static NamedFinder<Ingredient> find = new NamedFinder<>(Ingredient.class);

	public static List<Ingredient> byFilter(long[] functions, Page page) {
		if (functions.length == 0) {
			return page.apply(find.order().desc("popularity").order().asc("id"));
		}

		String query = "SELECT DISTINCT main.id as id, main.popularity " +
				"FROM " + TABLENAME + " main JOIN " + IngredientFunction.TABLENAME + " aux " +
				"ON main.id = aux.ingredient_id WHERE " +
				"aux.function_id IN (" + Util.joinString(",", functions) + ") " +
				"GROUP BY main.id " +
				"HAVING count(*) = " + functions.length + " " +
				"ORDER BY main.popularity DESC, main.id ASC ";

		List<Ingredient> filterList = page.apply(find, query);
		List<Ingredient> result = new ArrayList<>();
		for (Ingredient ingredient : filterList) {
			result.add(App.cache().ingredients.get(ingredient.getId()));
		}
		return result;
	}
}
