package src.models.userdata;

import src.models.util.BaseModel;
import src.models.data.Product;

import javax.persistence.*;

@Entity
@Table(name = RoutineItem.TABLENAME)
public class RoutineItem extends BaseModel {
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "routine_id", referencedColumnName = "id")
	private Routine routine;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	//Getters

	public Routine getRoutine() {
		return routine;
	}

	public Product getProduct() {
		return product;
	}

	//Setters

	public void setRoutine(Routine routine) {
		this.routine = routine;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	//Static

	public static final String TABLENAME = "routine_item";
}
