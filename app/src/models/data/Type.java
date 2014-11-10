package src.models.data;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import src.App;
import src.models.util.BaseModel;
import src.models.util.NamedFinder;
import src.models.util.NamedModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = Type.TABLENAME)
public class Type extends NamedModel {
	private long parent_id;

	//Get/Set

	public long getParent_id() {
		return parent_id;
	}

	public void setParent_id(long parent_id) {
		this.parent_id = parent_id;
	}

	//Parent relation

	public Type getParent() {
		return App.cache().types.get(parent_id);
	}

	public void setParent(Type parent) {
		this.parent_id = BaseModel.getIdIfExists(parent);
	}

	//Many-Many Products relation

	public Set<ProductType> getProductTypes() {
		return App.cache().product_type.getByR(getId());
	}

	public TLongSet getProducts() {
		Set<ProductType> result = getProductTypes();
		return App.cache().product_type.getIdsL(new TLongHashSet(), result);
	}

	//Static

	public static final String TABLENAME = "type";

	public static NamedFinder<Type> find = new NamedFinder<>(Type.class);
}
