package src.models.util;

import play.db.ebean.Model;
import src.models.MemCache;

public class BaseFinder<T extends BaseModel> extends Model.Finder<Long, T> implements MemCache.Getter<T> {
	public BaseFinder(Class<T> clazz) {
		super(Long.class, clazz);
	}
}
