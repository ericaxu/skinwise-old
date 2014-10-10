package src.models;

import play.db.ebean.Model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseModel extends Model {
	@Id
	private long id;

	public long getId() {
		return id;
	}
}
