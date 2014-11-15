package src.models.data;

import com.avaje.ebean.Ebean;
import gnu.trove.list.TLongList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.apache.commons.lang3.text.WordUtils;
import src.App;
import src.models.MemCache;
import src.models.util.*;
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

	private transient ManyToManyHistory<IngredientFunction> functions = new IngredientFunctionHistory();

	public TLongSet getFunctionIds() {
		return functions.getOtherIds(getId());
	}

	public void setFunctionIds(TLongSet type_ids) {
		functions.setOtherIds(type_ids);
	}

	public Set<Function> getFunctions() {
		return App.cache().functions.getSet(getFunctionIds().toArray());
	}

	public void setFunctions(Set<Function> input) {
		setFunctionIds(App.cache().functions.getIdSet(input));
	}

	//Many-Many Benefits relation

	private transient ManyToManyHistory<IngredientBenefit> benefits = new IngredientBenefitHistory();

	public TLongSet getBenefitIds() {
		return benefits.getOtherIds(getId());
	}

	public void setBenefitIds(TLongSet type_ids) {
		benefits.setOtherIds(type_ids);
	}

	public Set<Benefit> getBenefits() {
		return App.cache().benefits.getSet(getBenefitIds().toArray());
	}

	public void setBenefits(Set<Benefit> input) {
		setBenefitIds(App.cache().benefits.getIdSet(input));
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

			functions.flush(getId());
			benefits.flush(getId());

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
		query.from(TABLENAME + " main JOIN " + IngredientFunction.TABLENAME + " aux ON main.id = aux.left_id");

		if (functions.length > 0) {
			query.where("aux.right_id IN (" + Util.joinString(",", functions) + ")");
			query.other("GROUP BY main.id");
			query.other("HAVING count(*) = " + functions.length);
		}

		query.other("ORDER BY main.popularity DESC, main.id ASC");

		TLongList result = query.execute();
		TLongSet filter = new TLongHashSet();
		result = page.filter(result, filter);

		return App.cache().ingredients.getList(result.toArray());
	}

	public static class IngredientFunctionHistory extends ManyToManyHistory<IngredientFunction> {
		@Override
		protected MemCache.ManyToManyIndex<IngredientFunction> getIndex() {
			return App.cache().ingredient_function;
		}

		@Override
		protected IngredientFunction create(long ingredient_id, long function_id) {
			IngredientFunction object = new IngredientFunction();
			object.setLeft_id(ingredient_id);
			object.setRight_id(function_id);
			return object;
		}
	}

	public static class IngredientBenefitHistory extends ManyToManyHistory<IngredientBenefit> {
		@Override
		protected MemCache.ManyToManyIndex<IngredientBenefit> getIndex() {
			return App.cache().ingredient_benefit;
		}

		@Override
		protected IngredientBenefit create(long ingredient_id, long benefit_id) {
			IngredientBenefit object = new IngredientBenefit();
			object.setLeft_id(ingredient_id);
			object.setRight_id(benefit_id);
			return object;
		}
	}
}
