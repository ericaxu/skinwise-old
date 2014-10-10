package src.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Ingredient extends BaseModel {

	@Column(length = 1024)
	private String name;

	@Column(length = 128)
	private String cas_number;

	@Column(length = 4096)
	private String description;

	@OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER)
	private Set<IngredientName> names = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Function> functions = new HashSet<>();

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

	public void loadFrom(Ingredient other) {
		setName(other.getName());
		setCas_number(other.getCas_number());
		setDescription(other.getDescription());
		setNames(other.getNames());
		setFunctions(other.getFunctions());
	}

	public List<String> getFunctionNames() {
		List<String> functionNames = new ArrayList<>();
		for (Function function : functions) {
			functionNames.add(function.getName());
		}
		return functionNames;
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

	public static List<Ingredient> getAll() {
		return find.all();
	}
}
