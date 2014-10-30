package src.models.data;

import gnu.trove.list.TLongList;
import gnu.trove.set.TLongSet;
import src.App;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = Brand.TABLENAME)
public class Brand extends NamedModel {
	//Products relation

	public TLongList getProducts() {
		return App.cache().brand_product.getMany(this.getId());
	}

	//Static
	public static final String TABLENAME = "brand";
	public static NamedFinder<Brand> find = new NamedFinder<>(Brand.class);
}
