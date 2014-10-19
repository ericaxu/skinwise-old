package src.models.data;

import src.models.Page;
import src.util.Util;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Product.TABLENAME)
public class Product extends NamedModel {
	private int popularity;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "brand_id", referencedColumnName = "id")
	private Brand brand;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "type_id", referencedColumnName = "id")
	private ProductType type;

	@Column(length = 1023)
	private String line;

	@Column(length = 1023)
	private String image;

	//Relation table

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, mappedBy = "product")
	private List<ProductIngredient> ingredient_links = new ArrayList<>();

	//Getters

	public int getPopularity() {
		return popularity;
	}

	public Brand getBrand() {
		return brand;
	}

	public ProductType getType() {
		return type;
	}

	public String getLine() {
		return line;
	}

	public String getImage() {
		return image;
	}

	public List<ProductIngredient> getIngredientLinks() {
		return ingredient_links;
	}

	//Setters

	public void setPopularity(int popularity) {
		this.popularity = popularity;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	public void setType(ProductType type) {
		this.type = type;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setIngredientLinks(List<ProductIngredient> ingredient_links) {
		this.ingredient_links = ingredient_links;
		ingredients_cache = null;
		key_ingredients_cache = null;
	}

	//Others

	public String getBrandName() {
		if (brand == null) {
			return "";
		}
		return brand.getName();
	}

	public String getTypeName() {
		if (type == null) {
			return "";
		}
		return type.getName();
	}

	private transient List<IngredientName> ingredients_cache;
	private transient List<IngredientName> key_ingredients_cache;

	public List<IngredientName> getIngredients() {
		if (ingredients_cache == null) {
			ingredients_cache = new ArrayList<>();
			List<ProductIngredient> list = getIngredientLinks();
			Collections.sort(list, ProductIngredient.sorter);
			for (ProductIngredient link : list) {
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
			List<ProductIngredient> list = getIngredientLinks();
			Collections.sort(list, ProductIngredient.sorter);
			for (ProductIngredient link : list) {
				if (link.isIs_key()) {
					key_ingredients_cache.add(link.getIngredient_name());
				}
			}
		}
		return key_ingredients_cache;
	}

	public void setIngredientList(List<IngredientName> ingredients,
	                              List<IngredientName> key_ingredients) {

		List<ProductIngredient> old_links = getIngredientLinks();
		for (ProductIngredient link : old_links) {
			link.delete();
		}

		List<ProductIngredient> ingredient_links = new ArrayList<>();
		int i = 0;

		for (IngredientName ingredient : ingredients) {
			ProductIngredient item = new ProductIngredient();
			item.setProduct(this);
			item.setIngredient_name(ingredient);
			item.setIs_key(false);
			item.setItem_order(i);
			i++;
			ingredient_links.add(item);
		}

		for (IngredientName ingredient : key_ingredients) {
			ProductIngredient item = new ProductIngredient();
			item.setProduct(this);
			item.setIngredient_name(ingredient);
			item.setIs_key(true);
			item.setItem_order(i);
			i++;
			ingredient_links.add(item);
		}

		setIngredientLinks(ingredient_links);
	}

	@Override
	public void refresh() {
		ingredients_cache = null;
		key_ingredients_cache = null;
		super.refresh();
	}

	//Static

	public static final String TABLENAME = "product";

	public static Finder<Long, Product> find = new Finder<>(Long.class, Product.class);

	public static List<Product> all() {
		return find.all();

	}

	public static Product byId(long id) {
		return find.byId(id);
	}

	public static Product byBrandAndName(Brand brand, String name) {
		return find.where()
				.eq("brand", brand)
				.eq("name", name)
				.findUnique();
	}

	public static List<Product> byFilter(long[] brands, long[] ingredients, Page page) {
		if (brands.length == 0 && ingredients.length == 0) {
			return page.apply(find.order().desc("popularity"));
		}

		String query = "SELECT DISTINCT main.id as id, main.popularity " +
				"FROM " + TABLENAME + " main JOIN " + ProductIngredient.TABLENAME + " aux " +
				"ON main.id = aux.product_id WHERE ";

		if (brands.length > 0) {
			query += " main.brand_id IN (" + Util.joinString(",", brands) + ") ";

			if (ingredients.length > 0) {
				query += "AND ";
			}
		}

		if (ingredients.length > 0) {
			List<Long> ingredient_name_ids = new ArrayList<>();
			for (long ingredient_id : ingredients) {
				Set<IngredientName> names = Ingredient.byId(ingredient_id).getNames();
				for (IngredientName name : names) {
					ingredient_name_ids.add(name.getId());
				}
			}
			Long[] list = ingredient_name_ids.toArray(new Long[ingredient_name_ids.size()]);

			query += " aux.ingredient_name_id IN (" + Util.joinString(",", list) + ") " +
					"GROUP BY main.id " +
					"HAVING count(*) = " + ingredients.length + " ";
		}

		query += " ORDER BY main.popularity DESC ";

		return page.apply(find, query);
	}

	public static List<Product> byBrand(Brand brand) {
		return find.where()
				.eq("brand", brand)
				.findList();
	}
}
