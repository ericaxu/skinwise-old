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

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, mappedBy = "product")
	private List<ProductIngredient> ingredient_links = new ArrayList<>();

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

	public boolean hasDescription() {
		return description.length() > 0;
	}

	public String getDescription() {
		return description;
	}

	public List<ProductIngredient> getIngredientLinks() {
		return ingredient_links;
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

	public void setIngredientLinks(List<ProductIngredient> ingredient_links) {
		this.ingredient_links = ingredient_links;
		ingredients_cache = null;
		key_ingredients_cache = null;
	}

	//Others

	private transient List<IngredientName> ingredients_cache;
	private transient List<IngredientName> key_ingredients_cache;

	public List<IngredientName> getIngredients() {
		if (ingredients_cache == null) {
			ingredients_cache = new ArrayList<>();
			for (ProductIngredient link : getIngredientLinks()) {
				if (!link.isIs_key()) {
					ingredients_cache.add(link.getIngredient_name());
				}
			}
		}
		return ingredients_cache;
	}

	public List<IngredientName> getKey_ingredients() {
		if (key_ingredients_cache == null) {
			key_ingredients_cache = new ArrayList<>();
			for (ProductIngredient link : getIngredientLinks()) {
				if (link.isIs_key()) {
					key_ingredients_cache.add(link.getIngredient_name());
				}

			}
		}
		return key_ingredients_cache;
	}

	@Override
	public void refresh() {
		ingredients_cache = null;
		key_ingredients_cache = null;
		super.refresh();
	}

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
