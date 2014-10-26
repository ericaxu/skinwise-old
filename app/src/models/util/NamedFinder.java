package src.models.util;

import src.models.MemCache;

public class NamedFinder<T extends NamedModel> extends BaseFinder<T> implements MemCache.NamedGetter<T> {
	public NamedFinder(Class<T> clazz) {
		super(clazz);
	}

	public T byName(String name) {
		return super.where().eq("name", name).findUnique();
	}
}
