package src.models.data;

import src.models.util.NamedFinder;
import src.models.util.NamedModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = Function.TABLENAME)
public class Function extends NamedModel {
	//Static
	public static final String TABLENAME = "function";
	public static NamedFinder<Function> find = new NamedFinder<>(Function.class);
}
