package src.models.util;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import src.models.MemCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ManyToManyHistory<T extends ManyToManyModel> {
	private static final String TAG = "ManyToManyHistory";

	private TLongSet other_cache;
	private TLongSet other_new;

	public TLongSet getOtherIds(long id) {
		if (other_cache == null) {
			Set<T> product_types = getIndex().getByL(id);
			other_cache = new TLongHashSet();
			for (T product_type : product_types) {
				other_cache.add(product_type.getRightId());
			}
		}

		return other_cache;
	}

	public void setOtherIds(TLongSet other_ids) {
		other_new = other_ids;
	}

	public void flush(long id) {
		if (other_new != null) {
			MemCache.ManyToManyIndex<T> index = getIndex();
			//Commit to DB first
			List<T> relationships_old = new ArrayList<>(index.getByL(id));
			for (T relationship : relationships_old) {
				relationship.delete();
			}
			List<T> relationships_new = new ArrayList<>();
			for (long other_id : other_new.toArray()) {
				T relationship = create(id, other_id);
				relationship.save();
				relationships_new.add(relationship);
			}

			//Commit to memcache
			for (T relationship : relationships_old) {
				index.remove(relationship);
			}
			for (T relationship : relationships_new) {
				index.add(relationship);
			}

			//Update cache
			other_cache = other_new;
			other_new = null;
		}
	}

	protected abstract MemCache.ManyToManyIndex<T> getIndex();

	protected abstract T create(long id, long other_id);
}
