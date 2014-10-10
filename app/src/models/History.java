package src.models;

import com.avaje.ebean.Ebean;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class History<T extends BaseModel> {
	public static final long TARGET_ID_NEW = -1L;
	public static final long SUBMITTED_BY_SYSTEM = -1L;

	@Column
	private long target_id;
	@Column
	private long submitted_by;
	@Column
	private long submitted_time;
	@Column
	private boolean approved;
	@Column
	private long approved_time;

	public History(long target_id, long submitted_by) {
		setTarget_id(target_id);
		setSubmitted_by(submitted_by);
		setSubmitted_time(System.currentTimeMillis());
	}

	public long getTarget_id() {
		return target_id;
	}

	public long getSubmitted_by() {
		return submitted_by;
	}

	public long getSubmitted_time() {
		return submitted_time;
	}

	public boolean isApproved() {
		return approved;
	}

	public long getApproved_time() {
		return approved_time;
	}

	public void setTarget_id(long target_id) {
		this.target_id = target_id;
	}

	public void setSubmitted_by(long submitted_by) {
		this.submitted_by = submitted_by;
	}

	public void setSubmitted_time(long submitted_time) {
		this.submitted_time = submitted_time;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public void setApproved_time(long approved_time) {
		this.approved_time = approved_time;
	}

	public void approve(T owner, T other) {
		setApproved(true);
		setApproved_time(System.currentTimeMillis());

		Ebean.beginTransaction();
		try {
			other.save();

			if (getTarget_id() == TARGET_ID_NEW) {
				setTarget_id(other.getId());
			}

			owner.save();
			Ebean.commitTransaction();
		}
		finally {
			Ebean.endTransaction();
		}
	}

	public static long getTargetId(BaseModel object) {
		if (object == null) {
			return TARGET_ID_NEW;
		}
		return object.getId();
	}
}
