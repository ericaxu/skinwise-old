package src.models.data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = Brand.TABLENAME)
public class Brand extends NamedModel {
	//Static

	public static final String TABLENAME = "brand";
	public static Finder<Long, Brand> find = new Finder<>(Long.class, Brand.class);

	public static List<Brand> all() {
		return find.all();
	}

	public static Brand byId(long id) {
		return find.byId(id);
	}

	public static Brand byName(String name) {
		return find.where()
				.eq("name", name)
				.findUnique();
	}
}
