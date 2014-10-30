package src.models.data;

import gnu.trove.list.TLongList;
import src.App;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = ProductType.TABLENAME)
public class ProductType extends NamedModel {
	//Products relation

	public TLongList getProducts() {
		return App.cache().type_product.getMany(this.getId());
	}

	//Static
	public static final String TABLENAME = "product_type";
	public static NamedFinder<ProductType> find = new NamedFinder<>(ProductType.class);
}
