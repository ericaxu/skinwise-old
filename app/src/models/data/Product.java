package src.models.data;

import com.avaje.ebean.Ebean;
import gnu.trove.list.TLongList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import src.App;
import src.models.MemCache;
import src.models.util.*;
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
public class Product extends PopularNamedModel {

	private long brand_id;
	private transient LongHistory brand_id_tracker = new LongHistory();

	@Column(length = 1023)
	private String line;

	@Column(length = 1023)
	private String image;

	private long price;
	private float size;
	private String size_unit;

	//Get/Set

	public long getBrand_id() {
		return brand_id_tracker.getValue(brand_id);
	}

	public String getLine() {
		return line;
	}

	public String getImage() {
		return image;
	}

	public long getPrice() {
		return price;
	}

	public float getSize() {
		return size;
	}

	public String getSize_unit() {
		return size_unit;
	}

	public void setBrand_id(long brand_id) {
		brand_id_tracker.setValue(this.brand_id, brand_id);
		this.brand_id = brand_id;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public void setSize_unit(String size_unit) {
		this.size_unit = size_unit;
	}

	//Many-One Brand relations

	public Brand getBrand() {
		return App.cache().brands.get(brand_id);
	}

	public void setBrand(Brand brand) {
		setBrand_id(BaseModel.getIdIfExists(brand));
	}

	//Many-Many Type relations

	private transient ManyToManyHistory<ProductType> types = new ProductTypeHistory();

	public TLongSet getTypesIds() {
		return types.getOtherIds(getId());
	}

	public void setTypesIds(TLongSet type_ids) {
		types.setOtherIds(type_ids);
	}

	public Set<Type> getTypes() {
		return App.cache().types.getSet(getTypesIds().toArray());
	}

	public void setTypes(Set<Type> input) {
		setTypesIds(App.cache().types.getIdSet(input));
	}

	//Many-Many Aliases relations

	private transient List<Alias> ingredients_cache;
	private transient List<Alias> ingredients_new;
	private transient List<Alias> key_ingredients_cache;
	private transient List<Alias> key_ingredients_new;

	public List<ProductIngredient> getProductIngredients() {
		Set<ProductIngredient> result = App.cache().product_ingredient.getByL(getId());
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

	//One-Many ProductProperty relation

	public TLongList getProductProperties() {
		return App.cache().product_product_properties.getMany(this.getId());
	}

	//Others

	public String getFormattedPrice() {
		return Util.formatPrice(getPrice());
	}

	public String getBrandName() {
		if (getBrand() == null) {
			return "";
		}
		return getBrand().getName();
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

			types.flush(getId());

			Ebean.commitTransaction();
		}
		finally {
			Ebean.endTransaction();
		}
		brand_id_tracker.flush(App.cache().brand_product, getId());
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

	public static List<Product> byFilter(long[] brands, long[] negBrands,
	                                     long[] types,
	                                     long[] ingredients, long[] nedIngredients,
	                                     long priceMin, long priceMax,
	                                     boolean discontinued,
	                                     Page page) {
		if (priceMax <= priceMin) {
			priceMax = Long.MAX_VALUE;
		}

		SelectQuery query = new SelectQuery();
		query.select("DISTINCT id, popularity");
		query.from(TABLENAME);

		//Discontinued products
		//		if (!discontinued) {
		//			query.where("main.name NOT LIKE '%Discontinued%'");
		//		}
		query.where("price BETWEEN " + priceMin + " AND " + priceMax);

		if (brands.length > 0) {
			query.where("brand_id IN (" + Util.joinString(",", brands) + ")");
		}

		if (negBrands.length > 0) {
			query.where("brand_id NOT IN (" + Util.joinString(",", negBrands) + ")");
		}

		query.other("ORDER BY popularity DESC, id ASC");

		TLongList result = query.execute();
		TLongSet filter = new TLongHashSet();
		TLongSet ingredients_intersect = null;
		TLongSet types_intersect = null;

		if (nedIngredients.length > 0) {
			TLongSet alias_ids = new TLongHashSet();
			for (long ingredient_id : nedIngredients) {
				TLongList aliases = App.cache().ingredient_alias.getMany(ingredient_id);
				alias_ids.addAll(aliases);
			}
			long[] list = alias_ids.toArray();

			SelectQuery q = new SelectQuery();
			q.select("DISTINCT product_id as id");
			q.from(ProductIngredient.TABLENAME);
			q.where("alias_id IN (" + Util.joinString(",", list) + ")");

			filter.addAll(q.execute());
		}

		if (ingredients.length > 0) {
			TLongSet alias_ids = new TLongHashSet();
			for (long ingredient_id : ingredients) {
				TLongList aliases = App.cache().ingredient_alias.getMany(ingredient_id);
				alias_ids.addAll(aliases);
			}
			long[] list = alias_ids.toArray();

			SelectQuery q = new SelectQuery();
			q.select("DISTINCT product_id as id");
			q.from(ProductIngredient.TABLENAME);
			q.where("alias_id IN (" + Util.joinString(",", list) + ")");
			q.other("GROUP BY product_id");
			q.other("HAVING count(*) >= " + ingredients.length);

			ingredients_intersect = new TLongHashSet(q.execute());
		}

		if (types.length > 0) {
			SelectQuery q = new SelectQuery();
			q.select("DISTINCT product_id as id");
			q.from(ProductType.TABLENAME);
			q.where("type_id IN (" + Util.joinString(",", types) + ")");
			//			q.other("GROUP BY product_id");
			//			q.other("HAVING count(*) >= " + types.length);

			types_intersect = new TLongHashSet(q.execute());
		}

		result = page.filter(result, filter, ingredients_intersect, types_intersect);

		return App.cache().products.getList(result.toArray());
	}

	public static class ProductTypeHistory extends ManyToManyHistory<ProductType> {
		@Override
		protected MemCache.ManyToManyIndex<ProductType> getIndex() {
			return App.cache().product_type;
		}

		@Override
		protected ProductType create(long id, long other_id) {
			ProductType object = new ProductType();
			object.setProduct_id(id);
			object.setType_id(other_id);
			return object;
		}
	}
}
