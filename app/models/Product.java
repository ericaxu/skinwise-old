package models;

import play.db.ebean.Model;

import javax.persistence.*;
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

	public Product(String brand,
	                  String line,
	                  String name) {
		this.name = name;
		this.brand = brand;
		this.line = line;
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
