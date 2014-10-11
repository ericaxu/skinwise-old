package src.models.data.product;

import src.models.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
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

	@Column(length = 8192)
	private String ingredients;

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

	public String getIngredients() {
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

	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}

	//Others

	public void loadFrom(Product other) {
		setBrand(other.getBrand());
		setName(other.getName());
		setName(other.getName());
		setDescription(other.getDescription());
		setIngredients(other.getIngredients());
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
