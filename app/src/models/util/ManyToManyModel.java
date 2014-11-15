package src.models.util;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ManyToManyModel extends BaseModel {
	private long left_id;
	private long right_id;

	//Get/Set

	public long getLeft_id() {
		return left_id;
	}

	public long getRight_id() {
		return right_id;
	}

	public void setLeft_id(long left_id) {
		this.left_id = left_id;
	}

	public void setRight_id(long right_id) {
		this.right_id = right_id;
	}
}
