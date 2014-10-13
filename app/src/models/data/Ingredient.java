package src.models.data;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import src.models.BaseModel;
import src.models.Page;

import javax.persistence.*;
import java.util.*;

@Entity
public class Ingredient extends BaseModel {

	@Column(length = 1024)
	private String name;

	@Column(length = 128)
	private String cas_number;

	@Column(length = 4096)
	private String description;

	@Column(length = 4096)
	private String functions_string;

	//Relation table

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private Set<Function> functions = new HashSet<>();

	// Non-columns

	@OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<IngredientName> names = new HashSet<>();

	//Getters

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return WordUtils.capitalizeFully(name);
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

		//This is used for search
		List<String> function_list = new ArrayList<>();
		for (Function f : functions) {
			function_list.add("[" + f.getId() + "]");
		}
		Collections.sort(function_list);
		this.functions_string = StringUtils.join("", function_list);
	}

	//Others

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
		String functions_string = "%";

		if (functions.length > 0) {
			List<String> function_list = new ArrayList<>();
			for (long id : functions) {
				function_list.add("[" + id + "]");
			}
			Collections.sort(function_list);
			functions_string = "%" + StringUtils.join("%", function_list) + "%";
		}

		return page.apply(find.where().ilike("functions_string", functions_string));
	}

	public static List<Ingredient> getAll() {
		return find.all();
	}
}
