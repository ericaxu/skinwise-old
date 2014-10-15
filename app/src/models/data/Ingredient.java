package src.models.data;

import com.avaje.ebean.RawSqlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import play.api.db.DB$;
import play.db.DB;
import src.models.BaseModel;
import src.models.Page;
import src.util.Logger;
import src.util.Util;

import javax.persistence.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Entity
public class Ingredient extends BaseModel {
	private static final String TAG = "Ingredient";

	@Column(length = 1024)
	private String name;

	@Column(length = 128)
	private String cas_number;

	@Column(length = 4096)
	private String description;

	//Relation table

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinTable(name = "ingredient_function")
	private Set<Function> functions = new HashSet<>();

	//Non-columns

	@OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<IngredientName> names = new HashSet<>();

	//Getters

	public String getName() {
		return name;
	}

	public String getCas_number() {
		return cas_number;
	}

	public String getDescription() {
		return description;
	}

	public Set<IngredientName> getNames() {
		return names;
	}

	public Set<Function> getFunctions() {
		return functions;
	}

	//Setters

	public void setName(String name) {
		this.name = name;
	}

	public void setCas_number(String cas_number) {
		this.cas_number = cas_number;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setNames(Set<IngredientName> names) {
		this.names = names;
	}

	public void setFunctions(Set<Function> functions) {
		this.functions = functions;
	}

	//Others

	public String getDisplayName() {
		return WordUtils.capitalizeFully(name);
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

	public static Finder<Long, Ingredient> find = new Finder<>(Long.class, Ingredient.class);

	public static Ingredient byId(long id) {
		return find.byId(id);
	}

	public static Ingredient byINCIName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}

	public static List<Ingredient> byName(String name) {
		return find.where()
				.like("name", name)
				.findList();
	}

	public static List<Ingredient> byFunctions(long[] functions, Page page) {
		// Return all ingredients
		if (functions.length == 0) {
			return page.apply(find.query());
		}
		String query_from = " FROM ingredient i WHERE " +
				"(SELECT COUNT(*) FROM ingredient_function WHERE " +
				"ingredient_id = i.id AND " +
				"function_id IN (" + Util.joinString(",", functions) + ") = " +
				functions.length + ")";

		try {
			page.count = Util.sqlCount(query_from);
		}
		catch (SQLException e) {
			Logger.fatal(TAG, "SQL Count Query failed", e);
		}

		return page.apply(find.setRawSql(RawSqlBuilder.parse("SELECT i.id" + query_from)
				.columnMapping("i.id", "id").create()), false);
	}

	public static List<Ingredient> all() {
		return find.all();
	}

	public static List<Ingredient> getAll() {
		return find.all();
	}
}
