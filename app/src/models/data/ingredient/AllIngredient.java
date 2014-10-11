package src.models.data.ingredient;

import src.models.data.History;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class AllIngredient extends Ingredient {
	@Embedded
	private History<Ingredient> history;

	public AllIngredient(long target_id, long submitted_by) {
		history = new History<>(target_id, submitted_by);
	}

	public History<Ingredient> getHistory() {
		return history;
	}

	public void approve() {
		Ingredient other = Ingredient.byId(history.getTarget_id());
		if (other == null) {
			other = new Ingredient();
		}
		other.loadFrom(this);
		history.approve(this, other);
	}

	//Static

	public static Finder<Long, AllIngredient> find = new Finder<>(Long.class, AllIngredient.class);

	public static AllIngredient byId(long id) {
		return find.byId(id);
	}

	public static List<AllIngredient> byTargetId(long target_id) {
		return find.where()
				.eq("target_id", target_id)
				.order().desc("submitted_time")
				.findList();
	}

	public static List<AllIngredient> byApprovedTargetId(long target_id) {
		return find.where()
				.eq("target_id", target_id)
				.eq("approved", true)
				.order().desc("submitted_time")
				.findList();
	}
}
