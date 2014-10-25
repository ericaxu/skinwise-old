package src.models.data;

import gnu.trove.set.TLongSet;
import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.Page;
import src.util.Logger;
import src.util.Util;

import javax.persistence.*;
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

	//Cached
	private transient List<IngredientFunction> pairs;
	private transient Set<Function> functions;

	//Getters

	public long getPopularity() {
		return popularity;
	}

	public String getCas_number() {
		return cas_number;
	}

	public Set<IngredientName> getNames() {
		TLongSet name_ids = App.cache().getNamesForIngredient(this.getId());
		Set<IngredientName> names = new HashSet<>();
		for (long name_id : name_ids.toArray()) {
			names.add(App.cache().ingredient_names.get(name_id));
		}
		return names;
	}

	//Setters

	public void setPopularity(long popularity) {
		this.popularity = popularity;
	}

	public void setCas_number(String cas_number) {
		this.cas_number = cas_number;
	}

	//Cached getter/setters

	private List<IngredientFunction> getPairs() {
		if (pairs == null) {
			pairs = IngredientFunction.byFunctionId(this.getId());
		}
		return pairs;
	}

	public Set<Function> getFunctions() {
		if (functions == null) {
			List<IngredientFunction> pairs = getPairs();
			functions = new HashSet<>();
			for (IngredientFunction pair : pairs) {
				functions.add(pair.getFunction());
			}
		}
		return functions;
	}

	public void saveFunctions(Set<Function> newFunctions) {
		List<IngredientFunction> oldPairs = getPairs();
		for (IngredientFunction oldPair : oldPairs) {
			oldPair.delete();
		}
		pairs.clear();
		functions = new HashSet<>();
		for (Function newFunction : newFunctions) {
			functions.add(newFunction);
			IngredientFunction pair = new IngredientFunction();
			pair.setFunction(newFunction);
			pair.setIngredient(this);
			pair.save();
			pairs.add(pair);
		}
	}

	//Others

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
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

	public List<String> getNamesString() {
		List<String> result = new ArrayList<>();
		for (IngredientName name : this.getNames()) {
			result.add(name.getName());
		}
		return result;
	}

	//Static

	public static final String TABLENAME = "ingredient";
	public static final String FUNCTIONS_JOINTABLE = "ingredient_function";

	public static Finder<Long, Ingredient> find = new Finder<>(Long.class, Ingredient.class);

	public static List<Ingredient> all() {
		return find.all();
	}

	public static Ingredient byId(long id) {
		return find.byId(id);
	}

	public static Ingredient byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}

	public static List<Ingredient> byFilter(long[] functions, Page page) {
		if (functions.length == 0) {
			return page.apply(find.order().desc("popularity").order().asc("id"));
		}

		String query = "SELECT DISTINCT main.id as id, main.popularity " +
				"FROM " + TABLENAME + " main JOIN " + FUNCTIONS_JOINTABLE + " aux " +
				"ON main.id = aux.ingredient_id WHERE " +
				"aux.function_id IN (" + Util.joinString(",", functions) + ") " +
				"GROUP BY main.id " +
				"HAVING count(*) = " + functions.length + " " +
				"ORDER BY main.popularity DESC, main.id ASC ";

		return page.apply(find, query);
	}
}
