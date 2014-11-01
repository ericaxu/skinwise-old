package src.models.util;

import src.models.MemCache;

import javax.persistence.Embeddable;

public class LongHistory {
	private static final String TAG = "LinkedLongValue";

	private long value_old = 0;
	private long value_new = 0;
	private boolean value_changed = false;

	public long getValue(long current) {
		//Changed, let's still use the old value until the new one is flushed to DB.
		if (value_changed) {
			return value_old;
		}
		return current;
	}

	public void setValue(long current, long value) {
		//Nothing changed, switch back to unchanged if necessary
		if (value_changed && value == value_old) {
			value_changed = false;
		}
		else if (value != current) {
			value_new = value;
			//Changed, keep a copy of old value
			if (!value_changed) {
				value_old = current;
				value_changed = true;
			}
		}
	}

	public void flush(MemCache.OneToManyIndex<? extends BaseModel> index, long id) {
		if (value_changed) {
			index.remove(id, value_old);
			index.add(id, value_new);
			value_changed = false;
		}
	}
}
