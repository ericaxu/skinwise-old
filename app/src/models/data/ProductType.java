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

	//Relations

	public Product getProduct() {
		return App.cache().products.get(getLeft_id());
	}

	public Type getType() {
		return App.cache().types.get(getRight_id());
	}

	public void setProduct(Product product) {
		setLeft_id(BaseModel.getIdIfExists(product));
	}

	public void setType(Type type) {
		setRight_id(BaseModel.getIdIfExists(type));
	}

	//Static

	public static final String TABLENAME = "product_type";

	public static BaseFinder<ProductType> find = new BaseFinder<>(ProductType.class);
}
