package src.models.ingredient;

import src.models.History;

import javax.persistence.Embedded;
import javax.persistence.Entity;

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
}
