package src.models.data;

import com.avaje.ebean.annotation.Index;
import src.App;
import src.models.util.BaseFinder;
import src.models.util.BaseModel;
import src.models.util.LongHistory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = ProductProperty.TABLENAME)
public class ProductProperty extends BaseModel {
	public static final String INGREDIENT_PERCENT = "ingredients.%d.percent";
	public static final String PRICE = "price";
	public static final String PRICE_PER_SIZE = "pricepersize";
	public static final String SIZE = "size";
	public static final String SUNSCREEN_SPF = "sunscreen.spf";

	@Index
	private long product_id;
	private transient LongHistory product_id_tracker = new LongHistory();

	@Index
	@Column(name = "_key", length = 255)
	private String key;

	@Column(length = 255)
	private String text_value;

	private double number_value;

	//Get/Set

	public long getProduct_id() {
		return product_id_tracker.getValue(product_id);
	}

	public String getKey() {
		return key;
	}

	public String getText_value() {
		return text_value;
	}

	public double getNumber_value() {
		return number_value;
	}

	public void setProduct_id(long product_id) {
		product_id_tracker.setValue(this.product_id, product_id);
		this.product_id = product_id;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setText_value(String text_value) {
		this.text_value = text_value;
	}

	public void setNumber_value(double number_value) {
		this.number_value = number_value;
	}

	//Many-One Product relation

	public Product getProduct() {
		return App.cache().products.get(getProduct_id());
	}

	public void setProduct(Product product) {
		setProduct_id(BaseModel.getIdIfExists(product));
	}

	//Others

	@Override
	public void save() {
		super.save();
		product_id_tracker.flush(App.cache().product_product_properties, getId());
	}

	//Static

	public static final String TABLENAME = "product_property";

	public static BaseFinder<ProductProperty> find = new BaseFinder<>(ProductProperty.class);
}
