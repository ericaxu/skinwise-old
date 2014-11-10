package src.models.data;

import src.App;
import src.models.util.BaseFinder;
import src.models.util.BaseModel;
import src.models.util.ManyToManyModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = ProductType.TABLENAME)
public class ProductType extends ManyToManyModel {
	private long product_id;
	private long type_id;

	//Get/Set

	public long getProduct_id() {
		return product_id;
	}

	public long getType_id() {
		return type_id;
	}

	public void setProduct_id(long product_id) {
		this.product_id = product_id;
	}

	public void setType_id(long type_id) {
		this.type_id = type_id;
	}

	//Relations

	public Product getProduct() {
		return App.cache().products.get(product_id);
	}

	public Type getType() {
		return App.cache().types.get(type_id);
	}

	public void setProduct(Product product) {
		setProduct_id(BaseModel.getIdIfExists(product));
	}

	public void setType(Type type) {
		setType_id(BaseModel.getIdIfExists(type));
	}

	//ManyToMany relations

	@Override
	public long getLeftId() {
		return getProduct_id();
	}

	@Override
	public long getRightId() {
		return getType_id();
	}

	//Static

	public static final String TABLENAME = "product_type";

	public static BaseFinder<ProductType> find = new BaseFinder<>(ProductType.class);
}
