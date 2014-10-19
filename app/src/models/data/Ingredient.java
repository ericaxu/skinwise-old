package src.models.data;

import org.apache.commons.lang3.text.WordUtils;
import src.models.Page;
import src.util.Util;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Ingredient.TABLENAME)
public class Ingredient extends NamedModel {
	private int popularity;

	@Column(length = 127)
	private String cas_number;

	//Relation table

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinTable(name = FUNCTIONS_JOINTABLE)
	private Set<Function> functions = new HashSet<>();

	//Non-columns

	@OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<IngredientName> names = new HashSet<>();

	//Getters

	public int getPopularity() {
		return popularity;
	}

	public String getCas_number() {
		return cas_number;
	}

	public Set<IngredientName> getNames() {
		return names;
	}

	public Set<Function> getFunctions() {
		return functions;
	}

	//Setters

	public void setPopularity(int popularity) {
		this.popularity = popularity;
	}

	public void setCas_number(String cas_number) {
		this.cas_number = cas_number;
	}

	public void setNames(Set<IngredientName> names) {
		this.names = names;
	}

	public void setFunctions(Set<Function> functions) {
		this.functions = functions;
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

	public List<String> getNamesString() {
		List<String> result = new ArrayList<>();
		for (IngredientName name : names) {
			result.add(name.getName());
		}
		return result;
	}

	//Static

	public static final String TABLENAME = "ingredient";
	public static final String FUNCTIONS_JOINTABLE = "ingredient_function";

	public static Finder<Long, Ingredient> find = new Finder<>(Long.class, Ingredient.class);

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
			return page.apply(find.order().desc("popularity"));
		}

		String query = "SELECT DISTINCT main.id as id, main.popularity " +
				"FROM " + TABLENAME + " main JOIN " + FUNCTIONS_JOINTABLE + " aux " +
				"ON main.id = aux.ingredient_id WHERE " +
				"aux.function_id IN (" + Util.joinString(",", functions) + ") " +
				"GROUP BY main.id " +
				"HAVING count(*) = " + functions.length + " " +
				"ORDER BY main.popularity DESC ";

		return page.apply(find, query);
	}

	public static List<Ingredient> all() {
		return find.all();
	}

	public static List<Ingredient> getAll() {
		return find.all();
	}
}
