package src.models.data;

import com.avaje.ebean.Ebean;
import gnu.trove.list.TLongList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.util.NamedFinder;
import src.models.util.Page;
import src.models.util.PopularNamedModel;
import src.models.util.SelectQuery;
import src.util.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Ingredient.TABLENAME)
public class Ingredient extends PopularNamedModel {

	private boolean active;

	@Column(length = 127)
	private String cas_number;

	//Get/Set

	public boolean isActive() {
		return active;
	}

	public String getCas_number() {
		return cas_number;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setCas_number(String cas_number) {
		this.cas_number = cas_number;
	}

	//Many-Many Functions relation

	private Set<IngredientFunction> getIngredient_functions() {
		return App.cache().ingredient_function.getByL(getId());
	}

	private transient TLongSet functions_cache;
	private transient TLongSet functions_new;

	public TLongSet getFunctionIds() {
		if (functions_cache == null) {
			Set<IngredientFunction> ingredient_functions = getIngredient_functions();
			functions_cache = new TLongHashSet();
			for (IngredientFunction ingredient_function : ingredient_functions) {
				functions_cache.add(ingredient_function.getFunction_id());
			}
		}

		return functions_cache;
	}

	public void setFunctionIds(TLongSet function_ids) {
		functions_new = function_ids;
	}

	public Set<Function> getFunctions() {
		return App.cache().functions.getSet(getFunctionIds().toArray());
	}

	public void setFunctions(Set<Function> input) {
		setFunctionIds(App.cache().functions.getIdSet(input));
	}

	//One-Many Aliases relation

	public TLongList getAliases() {
		return App.cache().ingredient_alias.getMany(this.getId());
	}

	//Others

	public List<String> getAliasesString() {
		List<String> result = new ArrayList<>();
		for (long aliasId : this.getAliases().toArray()) {
			result.add(App.cache().alias.get(aliasId).getName());
		}
		return result;
	}

	public String getDisplayName() {
		return WordUtils.capitalizeFully(getName());
	}

	public TLongSet getProducts() {
		TLongSet results = new TLongHashSet();
		for (long aliasId : this.getAliases().toArray()) {
			results.addAll(App.cache().alias.get(aliasId).getProducts());
		}
		return results;
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
				for (long function_id : functions_new.toArray()) {
					IngredientFunction ingredient_function = new IngredientFunction();
					ingredient_function.setFunction_id(function_id);
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
		SelectQuery query = new SelectQuery();
		query.select("DISTINCT main.id as id, main.popularity");
		query.from(TABLENAME + " main JOIN " + IngredientFunction.TABLENAME + " aux ON main.id = aux.ingredient_id");

		if (functions.length > 0) {
			query.where("aux.function_id IN (" + Util.joinString(",", functions) + ")");
			query.other("GROUP BY main.id");
			query.other("HAVING count(*) = " + functions.length);
		}

		query.other("ORDER BY main.popularity DESC, main.id ASC");

		TLongList result = query.execute();
		TLongSet filter = new TLongHashSet();
		result = page.filter(result, filter);

		return App.cache().ingredients.getList(result.toArray());
	}
}
