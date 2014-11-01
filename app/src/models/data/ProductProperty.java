package src.models.data;

import com.avaje.ebean.annotation.Index;
import src.App;
import src.models.util.BaseFinder;
import src.models.util.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ProductProperty extends BaseModel {
	@Index
	private long product_id;
	@Index
	@Column(name = "_key", length = 255)
	private String key;

	@Column(length = 255)
	private String value;

	//Getters

	public long getProduct_id() {
		return product_id;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	//Setters

	public void setProduct_id(long product_id) {
		this.product_id = product_id;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	//Others

	public Product getProduct() {
		return App.cache().products.get(getProduct_id());
	}

	public void setProduct(Product product) {
		setProduct_id(BaseModel.getIdIfExists(product));
	}

	//Static
	public static final String TABLENAME = "alias";
	public static BaseFinder<ProductProperty> find = new BaseFinder<>(ProductProperty.class);

	public ProductProperty byProductIdAndKey(long product_id, String key) {
		return find.where().eq("product_id", product_id).eq("key", key).findUnique();
	}
}
