package src.models.data;

import com.avaje.ebean.Ebean;
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

	//Functions relation

	private Set<IngredientFunction> getIngredient_functions() {
		return App.cache().ingredient_function.getL(getId());
	}

	private transient Set<Function> functions_cache;
	private transient Set<Function> functions_new;

	public Set<Function> getFunctions() {
		if (functions_cache == null) {
			Set<IngredientFunction> ingredient_functions = getIngredient_functions();
			functions_cache = new HashSet<>();
			for (IngredientFunction ingredient_function : ingredient_functions) {
				functions_cache.add(ingredient_function.getFunction());
			}
		}

		return functions_cache;
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

	public void setFunctions(Set<Function> input) {
		functions_new = input;
	}

	//ALiases relation

	public List<Alias> getAliases() {
		return App.cache().ingredient_alias.getManyObject(this.getId());
	}

	public List<String> getAliasesString() {
		List<String> result = new ArrayList<>();
		for (Alias name : this.getAliases()) {
			result.add(name.getName());
		}
		return result;
	}

	//Others

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	@Override
	public void save() {
		Ebean.beginTransaction();
		try {
			super.save();

			if (functions_new != null) {
				//Commit to DB first
				List<IngredientFunction> ingredient_functions_old = new ArrayList<>(getIngredient_functions());
				for (IngredientFunction ingredient_function : ingredient_functions_old) {
					ingredient_function.delete();
				}
				List<IngredientFunction> ingredient_functions_new = new ArrayList<>();
				for (Function function : functions_new) {
					IngredientFunction ingredient_function = new IngredientFunction();
					ingredient_function.setFunction(function);
					ingredient_function.setIngredient(this);
					ingredient_function.save();
					ingredient_functions_new.add(ingredient_function);
				}

				//Commit to memcache
				for (IngredientFunction ingredient_function : ingredient_functions_old) {
					App.cache().ingredient_function.remove(ingredient_function);
				}
				for (IngredientFunction ingredient_function : ingredient_functions_new) {
					App.cache().ingredient_function.add(ingredient_function);
				}

				//Update cache
				functions_cache = functions_new;
				functions_new = null;
			}

			Ebean.commitTransaction();
		}
		finally {
			Ebean.endTransaction();
		}
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
