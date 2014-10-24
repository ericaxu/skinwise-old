package src.models.data;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = ProductType.TABLENAME)
public class ProductType extends NamedModel {
	//Static

	public static final String TABLENAME = "product_type";
	public static Model.Finder<Long, ProductType> find = new Model.Finder<>(Long.class, ProductType.class);

	public static List<ProductType> all() {
		return find.all();
	}

	public static ProductType byId(long id) {
		return find.byId(id);
	}

	public static ProductType byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}
}
