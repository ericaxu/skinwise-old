package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ingredient extends Model {
	@Id
	private long id;

	@Column(length = 1024, unique = true)
	private String name;

	@Column(length = 128)
	private String cas_number;

	@Column(length = 1024)
	private String short_desc;

	@OneToMany(mappedBy = "ingredient", fetch = FetchType.EAGER)
	private List<IngredientName> names = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	private List<IngredientFunction> functions = new ArrayList<>();

	public Ingredient(String name,
	                  String cas_number,
	                  String short_desc,
	                  List<IngredientName> names,
	                  List<IngredientFunction> functions) {
		this.name = name;
		this.cas_number = cas_number;
		this.short_desc = short_desc;
		this.names = names;
		this.functions = functions;
	}

	public List<IngredientFunction> getFunctions() {
		return functions;
	}

	public List<String> getFunctionNames() {
		List<String> functionNames = new ArrayList<>();
		for (IngredientFunction function : functions) {
			functionNames.add(function.getName());
		}
		return functionNames;
	}

	public List<IngredientName> getNames() {
		return names;
	}

	public String getShort_desc() {
		return short_desc;
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
