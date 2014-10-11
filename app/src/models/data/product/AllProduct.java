package src.models.data.product;

import src.models.data.History;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class AllProduct extends Product {
	@Embedded
	private History<Product> history;

	public AllProduct(long target_id, long submitted_by) {
		history = new History<>(target_id, submitted_by);
	}

	public History<Product> getHistory() {
		return history;
	}

	public void approve() {
		Product other = Product.byId(history.getTarget_id());
		if (other == null) {
			other = new Product();
		}
		other.loadFrom(this);
		history.approve(this, other);
	}

	//Static

	public static Finder<Long, AllProduct> find = new Finder<>(Long.class, AllProduct.class);

	public static AllProduct byId(long id) {
		return find.byId(id);
	}

	public static List<AllProduct> byTargetId(long target_id) {
		return find.where()
				.eq("target_id", target_id)
				.order().desc("submitted_time")
				.findList();
	}

	public static List<AllProduct> byApprovedTargetId(long target_id) {
		return find.where()
				.eq("target_id", target_id)
				.eq("approved", true)
				.order().desc("submitted_time")
				.findList();
	}
}
