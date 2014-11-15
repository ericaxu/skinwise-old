package src.util;

import gnu.trove.TLongCollection;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

public class TLongIntersectSet {
	private TLongSet data = null;

	public void intersect(TLongCollection data) {
		if (this.data == null) {
			this.data = new TLongHashSet(data);
		}
		else {
			this.data.retainAll(data);
		}
	}

	public TLongSet get() {
		return data;
	}
}
