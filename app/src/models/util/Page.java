package src.models.util;

import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.List;

public class Page {
	private static final String TAG = "Page";

	public int page;
	public int size;
	public int count;

	public Page(int page) {
		this(page, 20);
	}

	public Page(int page, int size) {
		this.page = page;
		this.size = size;
		this.count = 0;
	}

	public TLongList filter(TLongList input, TLongSet no, TLongSet... intersects) {
		int start = Math.min(page * size, input.size());
		int end = Math.min(start + size, input.size());

		TLongSet yes = new TLongHashSet(input);
		for (TLongSet intersect : intersects) {
			if (intersect != null) {
				yes.retainAll(intersect);
			}
		}

		//No result
		if (start >= end || yes.isEmpty()) {
			count = 0;
			return new TLongArrayList();
		}

		TLongSet all = new TLongHashSet(yes);
		all.addAll(no);
		count = all.size() - no.size();

		TLongList result = new TLongArrayList(end - start);
		for (long id : input.toArray()) {
			if (!no.contains(id) && yes.contains(id)) {
				start--;
				end--;
				//Stop looking when we have enough
				if (end < 0) {
					break;
				}
				if (start < 0) {
					result.add(id);
				}
			}
		}
		return result;
	}

	public <T> List<T> apply(Query<T> query) {
		return apply(query, true);
	}

	public <T> List<T> apply(Query<T> query, boolean doCount) {
		return apply(query.findPagedList(page, size), doCount);
	}

	public <T> List<T> apply(PagedList<T> list, boolean doCount) {
		if (doCount) {
			count = list.getTotalRowCount();
		}
		return list.getList();
	}
}
