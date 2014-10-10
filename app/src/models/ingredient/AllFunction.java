package src.models.ingredient;

import src.models.History;

import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
public class AllFunction extends Function {
	@Embedded
	private History<Function> history;

	public AllFunction(long target_id, long submitted_by) {
		history = new History<>(target_id, submitted_by);
	}

	public History<Function> getHistory() {
		return history;
	}

	public void approve() {
		Function other = Function.byId(history.getTarget_id());
		if (other == null) {
			other = new Function();
		}
		other.loadFrom(this);
		history.approve(this, other);
	}
}
