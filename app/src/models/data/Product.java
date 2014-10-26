package src.models.data;

import com.avaje.ebean.Ebean;
import gnu.trove.list.TLongList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import src.App;
import src.models.util.BaseModel;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;
import src.models.util.Page;
import src.util.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
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

	//Brand relations

	public Brand getBrand() {
		return App.cache().brands.get(brand_id);
	}

	public void setBrand(Brand brand) {
		setBrand_id(BaseModel.getIdIfExists(brand));
	}

	//ProductType relations

	public ProductType getType() {
		return App.cache().types.get(product_type_id);
	}

	public void setType(ProductType type) {
		setProduct_type_id(BaseModel.getIdIfExists(type));
	}

	//Aliases relations

	private transient List<Alias> ingredients_cache;
	private transient List<Alias> ingredients_new;
	private transient List<Alias> key_ingredients_cache;
	private transient List<Alias> key_ingredients_new;

	public List<ProductIngredient> getProductIngredients() {
		Set<ProductIngredient> result = App.cache().product_ingredient.getL(getId());
		List<ProductIngredient> list = new ArrayList<>(result);
		Collections.sort(list, ProductIngredient.sorter);
		return list;
	}

	public List<Alias> getIngredients() {
		if (ingredients_cache == null) {
			List<ProductIngredient> pairs = getProductIngredients();
			ingredients_cache = new ArrayList<>();
			for (ProductIngredient pair : pairs) {
				if (!pair.isIs_key()) {
					ingredients_cache.add(pair.getAlias());
				}
			}
		}
		return ingredients_cache;
	}

	public List<Alias> getKey_ingredients() {
		if (key_ingredients_cache == null) {
			List<ProductIngredient> pairs = getProductIngredients();
			key_ingredients_cache = new ArrayList<>();
			for (ProductIngredient pair : pairs) {
				if (pair.isIs_key()) {
					key_ingredients_cache.add(pair.getAlias());
				}
			}
		}
		return key_ingredients_cache;
	}

	public void setIngredients(List<Alias> ingredients) {
		this.ingredients_new = ingredients;
	}

	public void setKeyIngredients(List<Alias> key_ingredients) {
		this.key_ingredients_new = key_ingredients;
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
	public void save() {
		Ebean.beginTransaction();
		try {
			super.save();

			boolean ingredients_changed = ingredients_new != null;
			boolean key_ingredients_changed = key_ingredients_new != null;
			if (ingredients_changed || key_ingredients_changed) {

				//Commit to DB first
				List<ProductIngredient> product_ingredients = getProductIngredients();
				List<ProductIngredient> product_ingredients_old = new ArrayList<>();
				//Find changed old relations
				for (ProductIngredient product_ingredient : product_ingredients) {
					if ((ingredients_changed && !product_ingredient.isIs_key()) ||
							key_ingredients_changed && product_ingredient.isIs_key()) {
						product_ingredients_old.add(product_ingredient);
					}
				}
				//Delete old relations
				for (ProductIngredient product_ingredient : product_ingredients_old) {
					product_ingredient.delete();
				}
				//Create new relations
				List<ProductIngredient> product_ingredients_new = new ArrayList<>();
				if (ingredients_changed) {
					for (int i = 0; i < ingredients_new.size(); i++) {
						ProductIngredient product_ingredient = createProductIngredient(
								ingredients_new.get(i), false, i);
						product_ingredients_new.add(product_ingredient);
					}
				}
				if (key_ingredients_changed) {
					for (int i = 0; i < key_ingredients_new.size(); i++) {
						ProductIngredient product_ingredient = createProductIngredient(
								key_ingredients_new.get(i), true, i);
						product_ingredients_new.add(product_ingredient);
					}
				}

				//Commit to memcache
				for (ProductIngredient product_ingredient : product_ingredients_old) {
					App.cache().product_ingredient.remove(product_ingredient);
				}
				for (ProductIngredient product_ingredient : product_ingredients_new) {
					App.cache().product_ingredient.add(product_ingredient);
				}

				//Update cache
				ingredients_cache = ingredients_new;
				key_ingredients_cache = key_ingredients_new;
				ingredients_new = null;
				key_ingredients_new = null;
			}

			Ebean.commitTransaction();
		}
		finally {
			Ebean.endTransaction();
		}
	}

	private ProductIngredient createProductIngredient(Alias ingredient, boolean isKey, int order) {
		ProductIngredient product_ingredient = new ProductIngredient();
		product_ingredient.setAlias(ingredient);
		product_ingredient.setProduct(this);
		product_ingredient.setIs_key(isKey);
		product_ingredient.setItem_order(order);
		product_ingredient.save();
		return product_ingredient;
	}

	//Static

	public static final String TABLENAME = "product";
	public static NamedFinder<Product> find = new NamedFinder<>(Product.class);

	public static List<Product> byFilter(long[] brands, long[] types, long[] ingredient_ids, Page page) {
		List<Product> result;
		if (brands.length == 0 && types.length == 0 && ingredient_ids.length == 0) {
			result = page.apply(find.order().desc("popularity").order().asc("id"));
		}
		else {
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
					TLongList aliases = App.cache().ingredient_alias.getMany(ingredient_id);
					alias_ids.addAll(aliases);
				}
				long[] list = alias_ids.toArray();

				query += " aux.alias_id IN (" + Util.joinString(",", list) + ") " +
						"GROUP BY main.id " +
						"HAVING count(*) = " + ingredient_ids.length + " ";
			}

			query += " ORDER BY main.popularity DESC, main.id ASC ";
			result = page.apply(find, query);
		}

		return App.cache().products.getList(App.cache().products.getIds(result));
	}
}
