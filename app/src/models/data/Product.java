package src.models.data;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import src.App;
import src.models.Page;
import src.models.util.BaseModel;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;
import src.util.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

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

	public long getBrand_id() {
		return brand_id;
	}

	public long getProduct_type_id() {
		return product_type_id;
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

	public void setBrand_id(long brand_id) {
		this.brand_id = brand_id;
	}

	public void setProduct_type_id(long product_type_id) {
		this.product_type_id = product_type_id;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public void setImage(String image) {
		this.image = image;
	}

	//Relations

	public Brand getBrand() {
		return App.cache().brands.get(brand_id);
	}

	public ProductType getType() {
		return App.cache().types.get(product_type_id);
	}

	public void setBrand(Brand brand) {
		setBrand_id(BaseModel.getIdIfExists(brand));
	}

	public void setType(ProductType type) {
		setProduct_type_id(BaseModel.getIdIfExists(type));
	}

	//Ingredients

	private transient List<ProductIngredient> pairs;
	private transient List<Alias> ingredients;
	private transient List<Alias> key_ingredients;

	private List<ProductIngredient> getPairs() {
		if (pairs == null) {
			pairs = ProductIngredient.byProductId(this.getId());
		}
		return pairs;
	}

	public List<ProductIngredient> getIngredientLinks() {
		return getPairs();
	}

	public List<Alias> getIngredients() {
		if (ingredients == null) {
			List<ProductIngredient> pairs = getPairs();
			ingredients = new ArrayList<>();
			for (ProductIngredient pair : pairs) {
				if (!pair.isIs_key()) {
					ingredients.add(pair.getAlias());
				}
			}
		}
		return ingredients;
	}

	public List<Alias> getKey_ingredients() {
		if (key_ingredients == null) {
			List<ProductIngredient> pairs = getPairs();
			key_ingredients = new ArrayList<>();
			for (ProductIngredient pair : pairs) {
				if (pair.isIs_key()) {
					key_ingredients.add(pair.getAlias());
				}
			}
		}
		return key_ingredients;
	}

	public void saveIngredients(List<Alias> newIngredients,
	                            List<Alias> newKeyIngredients) {
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

	private void refreshIngredientList(List<Alias> newList, boolean is_key) {
		for (int i = 0; i < newList.size(); i++) {
			ProductIngredient pair = new ProductIngredient();
			pair.setAlias(newList.get(i));
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
	public static NamedFinder<Product> find = new NamedFinder<>(Product.class);

	public static List<Product> byFilter(long[] brands, long[] types, long[] ingredient_ids, Page page) {
		if (brands.length == 0 && types.length == 0 && ingredient_ids.length == 0) {
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

		if (ingredient_ids.length > 0) {
			if (needAnd) {
				query += " AND ";
			}
			TLongSet alias_ids = new TLongHashSet();
			for (long ingredient_id : ingredient_ids) {
				List<Alias> aliases = App.cache().ingredients.get(ingredient_id).getNames();
				for (Alias alias : aliases) {
					alias_ids.add(alias.getId());
				}
			}
			long[] list = alias_ids.toArray();

			query += " aux.alias_id IN (" + Util.joinString(",", list) + ") " +
					"GROUP BY main.id " +
					"HAVING count(*) = " + ingredient_ids.length + " ";
		}

		query += " ORDER BY main.popularity DESC, main.id ASC ";

		List<Product> result = page.apply(find, query);

		TLongList resultIds = new TLongArrayList();
		for (Product p : result) {
			resultIds.add(p.getId());
		}
		return App.cache().products.getList(resultIds.toArray());
	}
}
