package src.models.data;

import src.models.util.NamedFinder;
import src.models.util.NamedModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = Brand.TABLENAME)
public class Brand extends NamedModel {
	//Static
	public static final String TABLENAME = "brand";
	public static NamedFinder<Brand> find = new NamedFinder<>(Brand.class);
}
