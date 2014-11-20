package src.models.data;

import com.avaje.ebean.Ebean;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import src.App;
import src.models.MemCache;
import src.models.util.*;
import src.util.TLongIntersectSet;
import src.util.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity
@Table(name = Product.TABLENAME)
public class Product extends PopularNamedModel {

	private long brand_id;
	private transient LongHistory brand_id_tracker = new LongHistory();

	@Column(length = 1023)
	private String line;

	@Column(length = 1023)
	private String image;

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

	//Many-One Brand relations

	public Brand getBrand() {
		return App.cache().brands.get(brand_id);
	}

	public void setBrand(Brand brand) {
		setBrand_id(BaseModel.getIdIfExists(brand));
	}

	//Many-Many Type relations

	private transient ManyToManyHistory<ProductType> types = new ProductTypeHistory();

	public TLongSet getTypeIds() {
		return types.getOtherIds(getId());
	}

	public void setTypeIds(TLongSet type_ids) {
		types.setOtherIds(type_ids);
	}

	public Set<Type> getTypes() {
		return App.cache().types.getSet(getTypeIds().toArray());
	}

	public void setTypes(Set<Type> input) {
		setTypeIds(App.cache().types.getIdSet(input));
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

	public Map<String, ProductProperty> getProperties() {
		Map<String, ProductProperty> result = new HashMap<>();
		for (long id : getProductProperties().toArray()) {
			ProductProperty property = App.cache().product_properties.get(id);
			result.put(property.getKey(), property);
		}
		return result;
	}

	//Others

	public String getBrandName() {
		if (getBrand() == null) {
			return "";
		}
		return getBrand().getName();
	}

	public String getFormattedIngredientPercent(long ingredient_id) {
		String key = String.format(ProductProperty.INGREDIENT_PERCENT, ingredient_id);
		ProductProperty property = getProperties().get(key);

		if (property != null) {
			return " (" + property.getNumber_value() + "%)";
		}

		return "";
	}

	public String getFormattedPrice() {
		ProductProperty property = getProperties().get(ProductProperty.PRICE);
		if (property == null) {
			return "";
		}
		return property.getText_value();
	}

	public String getFormattedSize() {
		ProductProperty property = getProperties().get(ProductProperty.SIZE);
		if (property == null) {
			return "";
		}
		return Util.formatNumber(property.getNumber_value()) + " " + property.getText_value();
	}

	public String getFormattedPricePerSize() {
		ProductProperty price_per_size = getProperties().get(ProductProperty.PRICE_PER_SIZE);
		ProductProperty size = getProperties().get(ProductProperty.SIZE);
		if (price_per_size == null || size == null) {
			return "";
		}
		return Util.formatPricePerOz(price_per_size.getNumber_value(), size.getText_value());
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

	public static abstract class ProductPropertyFilter {
		public String key;

		public ProductPropertyFilter(String key) {
			this.key = key;
		}
	}

	public static class ProductPropertyNumberFilter extends ProductPropertyFilter {
		public double min;
		public double max;

		public ProductPropertyNumberFilter(String key, double min, double max) {
			super(key);
			this.min = min;
			this.max = max;
		}
	}

	public static class ProductPropertyTextFilter extends ProductPropertyFilter {
		public String text;

		public ProductPropertyTextFilter(String key, String text) {
			super(key);
			this.text = text;
		}
	}

	public static List<Product> byFilter(long[] brands, long[] negBrands,
	                                     long[] types, long[] benefits,
	                                     long[] ingredients, long[] nedIngredients,
	                                     List<ProductPropertyNumberFilter> property_number_filters,
	                                     List<ProductPropertyTextFilter> property_text_filters,
	                                     boolean discontinued,
	                                     Page page) {

		SelectQuery query = new SelectQuery();
		query.select("DISTINCT id, popularity");
		query.from(TABLENAME);

		//Discontinued products
		//		if (!discontinued) {
		//			query.where("main.name NOT LIKE '%Discontinued%'");
		//		}

		if (brands.length > 0) {
			query.where("brand_id IN (" + Util.joinString(",", brands) + ")");
		}

		if (negBrands.length > 0) {
			query.where("brand_id NOT IN (" + Util.joinString(",", negBrands) + ")");
		}

		query.other("ORDER BY popularity DESC, id ASC");

		TLongList result = query.execute();
		TLongSet negative_filter = new TLongHashSet();
		TLongIntersectSet positive_filter = new TLongIntersectSet();

		boolean hasNumberFilter = property_number_filters != null && property_number_filters.size() > 0;
		boolean hasTextFilter = property_text_filters != null && property_text_filters.size() > 0;
		boolean hasFilter = hasNumberFilter || hasTextFilter;
		if (hasFilter) {
			SelectQuery q = new SelectQuery();
			q.select("DISTINCT product_id as id");
			q.from(ProductProperty.TABLENAME);

			int filters = 0;

			if (hasNumberFilter) {
				for (ProductPropertyNumberFilter filter : property_number_filters) {
					if (filter.max < filter.min) {
						filter.max = Double.MAX_VALUE;
					}
					q.where("(_key = ? AND number_value BETWEEN ? AND ?)", "OR");
					q.input(filter.key).input(filter.min).input(filter.max);
				}
				filters += property_number_filters.size();
			}

			if (hasTextFilter) {
				for (ProductPropertyTextFilter filter : property_text_filters) {
					q.where("(_key = ? AND text_value = ?)", "OR");
					q.input(filter.key).input(filter.text);
				}
				filters += property_text_filters.size();
			}

			q.other("GROUP BY id");
			q.other("HAVING count(*) = " + filters);

			positive_filter.intersect(q.execute());
		}

		if (benefits.length > 0) {
			SelectQuery q = new SelectQuery();
			q.select("DISTINCT first.left_id as id");
			q.from(ProductIngredient.TABLENAME + " first INNER JOIN " +
					Alias.TABLENAME + " second INNER JOIN " +
					IngredientBenefit.TABLENAME + " third ON " +
					"first.right_id = second.id AND second.ingredient_id = third.left_id");
			q.where("third.right_id IN (" + Util.joinString(",", benefits) + ")");
			q.other("GROUP BY id");
			q.other("HAVING count(DISTINCT third.right_id) = " + benefits.length);

			positive_filter.intersect(q.execute());
		}

		if (ingredients.length > 0) {
			SelectQuery q = new SelectQuery();
			q.select("DISTINCT first.left_id as id");
			q.from(ProductIngredient.TABLENAME + " first INNER JOIN " +
					Alias.TABLENAME + " second ON " +
					"first.right_id = second.id");
			q.where("second.ingredient_id IN (" + Util.joinString(",", ingredients) + ")");
			q.other("GROUP BY id");
			q.other("HAVING count(DISTINCT second.ingredient_id) = " + ingredients.length);

			positive_filter.intersect(q.execute());
		}

		if (types.length > 0) {
			SelectQuery q = new SelectQuery();
			q.select("DISTINCT left_id as id");
			q.from(ProductType.TABLENAME);
			q.where("right_id IN (" + Util.joinString(",", types) + ")");

			positive_filter.intersect(q.execute());
		}

		if (nedIngredients.length > 0) {
			TLongSet alias_ids = new TLongHashSet();
			for (long ingredient_id : nedIngredients) {
				TLongList aliases = App.cache().ingredient_alias.getMany(ingredient_id);
				alias_ids.addAll(aliases);
			}
			long[] list = alias_ids.toArray();

			SelectQuery q = new SelectQuery();
			q.select("DISTINCT left_id as id");
			q.from(ProductIngredient.TABLENAME);
			q.where("right_id IN (" + Util.joinString(",", list) + ")");

			negative_filter.addAll(q.execute());
		}

		result = page.filter(result, negative_filter, positive_filter.get());

		return App.cache().products.getList(result.toArray());
	}

	public static List<Product> similar(Product product, int num) {
		TLongList key_ingredients = getIngredientIds(product.getKey_ingredients());
		TLongList ingredients = getIngredientIds(product.getIngredients());

		if (key_ingredients.isEmpty() && ingredients.isEmpty()) {
			return new ArrayList<>();
		}

		TLongSet all_ingredients = new TLongHashSet(key_ingredients);
		all_ingredients.addAll(ingredients);

		String case_in = "sum(CASE WHEN second.ingredient_id IN (" +
				Util.joinString(",", key_ingredients.toArray()) +
				") THEN 10 ELSE 1 END) as score";

		if (key_ingredients.isEmpty()) {
			case_in = "sum(1) as score";
		}

		SelectQuery q = new SelectQuery();
		q.select("DISTINCT first.left_id as id, " +
				case_in + ", " +
				"third.popularity");
		q.from(ProductIngredient.TABLENAME + " first INNER JOIN " +
				Alias.TABLENAME + " second INNER JOIN " +
				Product.TABLENAME + " third ON " +
				"first.right_id = second.id AND " +
				"first.left_id = third.id");

		q.where("second.ingredient_id IN (" + Util.joinString(",", all_ingredients.toArray()) + ")");

		q.where("first.left_id <> " + product.getId());
		q.other("GROUP BY id");
		q.other("ORDER BY score DESC, third.popularity DESC");
		q.other("LIMIT " + num);

		TLongList result = q.execute();

		return App.cache().products.getList(result.toArray());
	}

	private static TLongList getIngredientIds(List<Alias> aliases) {
		TLongList ingredients = new TLongArrayList();
		for (Alias alias : aliases) {
			long ingredient_id = alias.getIngredient_id();
			if (!BaseModel.isIdNull(ingredient_id)) {
				ingredients.add(ingredient_id);
			}
		}
		return ingredients;
	}

	public static class ProductTypeHistory extends ManyToManyHistory<ProductType> {
		@Override
		protected MemCache.ManyToManyIndex<ProductType> getIndex() {
			return App.cache().product_type;
		}

		@Override
		protected ProductType create(long product_id, long type_id) {
			ProductType object = new ProductType();
			object.setLeft_id(product_id);
			object.setRight_id(type_id);
			return object;
		}
	}
}
