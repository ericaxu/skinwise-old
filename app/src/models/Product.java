package src.models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
public class Product extends Model {
	@Id
	private long id;

	@Column(length = 1024)
	private String brand;

	@Column(length = 1024)
	private String line;

	@Column(length = 1024)
	private String name;

	@Column(length = 4096)
	private String description;

	public Product(String name, String brand, String description) {
		this.name = name;
		this.brand = brand;
		this.description = description;
	}

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

	public long getId() {
		return id;
	}

	//Static

	public static Finder<Long, Product> find = new Finder<>(Long.class, Product.class);

	public static Product byId(long id) {
		return find.byId(id);
	}

	public static List<Product> byBrand(String brand) {
		return find.where()
				.eq("brand", brand)
				.findList();
	}
}
