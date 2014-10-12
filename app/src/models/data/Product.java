package src.models.data;

import src.models.BaseModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Product extends BaseModel {
	@Column(length = 1024)
	private String brand;

	@Column(length = 1024)
	private String line;

	@Column(length = 1024)
	private String name;

	@Column(length = 4096)
	private String description;

	//Relation table

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<IngredientName> key_ingredients = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<IngredientName> ingredients = new ArrayList<>();

	//Getters

	public String getBrand() {
		return brand;
	}

	public String getLine() {
		return line;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<IngredientName> getKey_ingredients() {
		return key_ingredients;
	}

	public List<IngredientName> getIngredients() {
		return ingredients;
	}
	//Setters

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setKey_ingredients(List<IngredientName> key_ingredients) {
		this.key_ingredients = key_ingredients;
	}

	public void setIngredients(List<IngredientName> ingredients) {
		this.ingredients = ingredients;
	}

	//Others

	//Static

	public static Finder<Long, Product> find = new Finder<>(Long.class, Product.class);

	public static Product byId(long id) {
		return find.byId(id);
	}

	public static Product byBrandAndName(String brand, String name) {
		return find.where()
				.eq("brand", brand)
				.eq("name", name)
				.findUnique();
	}

	public static List<Product> byBrand(String brand) {
		return find.where()
				.eq("brand", brand)
				.findList();
	}
}
