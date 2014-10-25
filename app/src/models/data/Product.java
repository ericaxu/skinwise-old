package src.models.data;

import src.App;
import src.models.BaseModel;
import src.models.Page;
import src.util.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = Product.TABLENAME)
public class Product extends NamedModel {
	private long popularity;

	private long brand_id;

	private long product_type_id;

	@Column(length = 1023)
	private String line;

	@Column(length = 1023)
	private String image;

	//Getters

	public long getPopularity() {
		return popularity;
	}

	public Brand getBrand() {
		return App.cache().brands.get(brand_id);
	}

	public ProductType getType() {
		return App.cache().types.get(product_type_id);
	}

	public String getLine() {
		return line;
	}

	public String getImage() {
		return image;
	}

	//Setters

	public void setPopularity(long popularity) {
		this.popularity = popularity;
	}

	public void setBrand(Brand brand) {
		this.brand_id = BaseModel.getIdIfExists(brand);
	}

	public void setType(ProductType type) {
		this.product_type_id = BaseModel.getIdIfExists(type);
	}

	public void setLine(String line) {
		this.line = line;
	}

	public void setImage(String image) {
		this.image = image;
	}

	//Ingredients

	private transient List<ProductIngredient> pairs;
	private transient List<IngredientName> ingredients;
	private transient List<IngredientName> key_ingredients;

	private List<ProductIngredient> getPairs() {
		if (pairs == null) {
			pairs = ProductIngredient.byProductId(this.getId());
		}
		return pairs;
	}

	public List<ProductIngredient> getIngredientLinks() {
		return getPairs();
	}

	public List<IngredientName> getIngredients() {
		if (ingredients == null) {
			List<ProductIngredient> pairs = getPairs();
			ingredients = new ArrayList<>();
			for (ProductIngredient pair : pairs) {
				if (!pair.isIs_key()) {
					ingredients.add(pair.getIngredient_name());
				}
			}
		}
		return ingredients;
	}

	public List<IngredientName> getKey_ingredients() {
		if (key_ingredients == null) {
			List<ProductIngredient> pairs = getPairs();
			key_ingredients = new ArrayList<>();
			for (ProductIngredient pair : pairs) {
				if (pair.isIs_key()) {
					key_ingredients.add(pair.getIngredient_name());
				}
			}
		}
		return key_ingredients;
	}

	public void saveIngredients(List<IngredientName> newIngredients,
	                            List<IngredientName> newKeyIngredients) {
		List<ProductIngredient> oldPairs = getPairs();
		for (ProductIngredient oldPair : oldPairs) {
			oldPair.delete();
		}
		pairs.clear();
		ingredients = newIngredients;
		key_ingredients = newKeyIngredients;

		refreshIngredientList(newIngredients, false);
		refreshIngredientList(newKeyIngredients, true);
	}

	private void refreshIngredientList(List<IngredientName> newList, boolean is_key) {
		for (int i = 0; i < newList.size(); i++) {
			ProductIngredient pair = new ProductIngredient();
			pair.setIngredient_name(newList.get(i));
			pair.setProduct(this);
			pair.setIs_key(is_key);
			pair.setItem_order(i);
			pair.save();
			pairs.add(pair);
		}
	}

	//Others

	public String getBrandName() {
		if (getBrand() == null) {
			return "";
		}
		return getBrand().getName();
	}

	public String getTypeName() {
		if (getType() == null) {
			return "";
		}
		return getType().getName();
	}

	@Override
	public void refresh() {
		pairs = null;
		ingredients = null;
		key_ingredients = null;
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

	public static List<Product> byFilter(long[] brands, long[] types, long[] ingredients, Page page) {
		if (brands.length == 0 && types.length == 0 && ingredients.length == 0) {
			return page.apply(find.order().desc("popularity").order().asc("id"));
		}

		String query = "SELECT DISTINCT main.id as id, main.popularity " +
				"FROM " + TABLENAME + " main JOIN " + ProductIngredient.TABLENAME + " aux " +
				"ON main.id = aux.product_id WHERE ";

		boolean needAnd = false;

		if (brands.length > 0) {
			query += " main.brand_id IN (" + Util.joinString(",", brands) + ") ";
			needAnd = true;
		}

		if (types.length > 0) {
			if (needAnd) {
				query += " AND ";
			}
			query += " main.product_type_id IN (" + Util.joinString(",", types) + ") ";
			needAnd = true;
		}

		if (ingredients.length > 0) {
			if (needAnd) {
				query += " AND ";
			}
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

		query += " ORDER BY main.popularity DESC, main.id ASC ";

		return page.apply(find, query);
	}
}
