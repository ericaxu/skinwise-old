package src.models;

import play.db.ebean.Model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseModel extends Model {
	public static final long NEW_ID = -1;

	@Id
	private long id;

	public long getId() {
		return id;
	}

	public static long getIdIfExists(BaseModel object) {
		if (object == null) {
			return -1;
		}
		return object.getId();
	}
}
