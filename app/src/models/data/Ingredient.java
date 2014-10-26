package src.models.data;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
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

	private Set<IngredientFunction> getIngredient_functions() {
		return App.cache().ingredient_function.getL(getId());
	}

	private transient Set<Function> functions;

	public Set<Function> getFunctions() {
		if (functions == null) {
			Set<IngredientFunction> ingredient_functions = getIngredient_functions();
			functions = new HashSet<>();
			for (IngredientFunction ingredient_function : ingredient_functions) {
				functions.add(ingredient_function.getFunction());
			}
		}

		return functions;
	}

	public void saveFunctions(Set<Function> input) {
		Set<IngredientFunction> ingredient_functions = getIngredient_functions();
		for (IngredientFunction ingredient_function : ingredient_functions) {
			ingredient_function.delete();
			App.cache().ingredient_function.remove(ingredient_function);
		}

		functions = input;

		for (Function function : input) {
			IngredientFunction ingredient_function = new IngredientFunction();
			ingredient_function.setFunction(function);
			ingredient_function.setIngredient(this);
			ingredient_function.save();
			App.cache().ingredient_function.add(ingredient_function);
		}
	}

	public List<String> getFunctionsString() {
		List<String> result = new ArrayList<>();
		for (Function function : getFunctions()) {
			result.add(WordUtils.capitalizeFully(function.getName()));
		}
		return result;
	}

	public TLongList getFunctionIds() {
		TLongList result = new TLongArrayList();
		for (Function function : getFunctions()) {
			result.add(function.getId());
		}
		return result;
	}

	//Names

	public List<Alias> getNames() {
		return App.cache().ingredient_alias.getManyObject(this.getId());
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
		List<Ingredient> result;
		if (functions.length == 0) {
			result = page.apply(find.order().desc("popularity").order().asc("id"));
		}
		else {
			String query = "SELECT DISTINCT main.id as id, main.popularity " +
					"FROM " + TABLENAME + " main JOIN " + IngredientFunction.TABLENAME + " aux " +
					"ON main.id = aux.ingredient_id WHERE " +
					"aux.function_id IN (" + Util.joinString(",", functions) + ") " +
					"GROUP BY main.id " +
					"HAVING count(*) = " + functions.length + " " +
					"ORDER BY main.popularity DESC, main.id ASC ";

			result = page.apply(find, query);
		}
		return App.cache().ingredients.getList(App.cache().ingredients.getIds(result));
	}
}
