package src.models.data;

import src.models.util.NamedFinder;
import src.models.util.NamedModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = ProductType.TABLENAME)
public class ProductType extends NamedModel {
	//Static
	public static final String TABLENAME = "product_type";
	public static NamedFinder<ProductType> find = new NamedFinder<>(ProductType.class);
}
