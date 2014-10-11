package src.models.data.ingredient;

import src.models.data.History;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
public class AllIngredientName extends IngredientName {
	@Embedded
	private History<IngredientName> history;

	public AllIngredientName(long target_id, long submitted_by) {
		history = new History<>(target_id, submitted_by);
	}

	public History<IngredientName> getHistory() {
		return history;
	}

	public void approve() {
		IngredientName other = IngredientName.byId(history.getTarget_id());
		if (other == null) {
			other = new IngredientName();
		}
		other.loadFrom(this);
		history.approve(this, other);
	}
}
