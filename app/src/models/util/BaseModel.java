package src.models.util;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseModel extends Model {
	public static final long NEW_ID = -1;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public static long getIdIfExists(BaseModel object) {
		if (object == null) {
			return 0;
		}
		return object.getId();
	}

	public static boolean isIdNull(long id) {
		return id <= 0;
	}
}
