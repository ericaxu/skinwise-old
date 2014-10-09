package src.models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.*;

@Entity
public class Ingredient extends Model {
	@Id
	private long id;

	@Column(length = 1024, unique = true)
	private String name;

	@Column(length = 128)
	private String cas_number;

	@Column(length = 1024)
	private String description;

	@OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER)
	private Set<IngredientName> names = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	private Set<IngredientFunction> functions = new HashSet<>();

	public Ingredient(String name,
	                  String cas_number,
	                  String description) {
		this.name = name;
		this.cas_number = cas_number;
		this.description = description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean addNames(Collection<IngredientName> c) {
		return names.addAll(c);
	}

	public boolean addFunctions(Collection<IngredientFunction> c) {
		return functions.addAll(c);
	}

	public Set<IngredientFunction> getFunctions() {
		return functions;
	}

	public List<String> getFunctionNames() {
		List<String> functionNames = new ArrayList<>();
		for (IngredientFunction function : functions) {
			functionNames.add(function.getName());
		}
		return functionNames;
	}

	public Set<IngredientName> getNames() {
		return names;
	}

	public String getDescription() {
		return description;
	}

	public String getCas_number() {
		return cas_number;
	}

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
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
}
