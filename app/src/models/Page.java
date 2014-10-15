package src.models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagingList;
import com.avaje.ebean.Query;

import java.util.List;

public class Page {
	public int page;
	public int size;
	public int count;

	public Page(int page, int size) {
		this.page = page;
		this.size = size;
		this.count = 0;
	}

	public <T> List<T> apply(Query<T> query) {
		return apply(query, true);
	}

	public <T> List<T> apply(Query<T> query, boolean doCount) {
		return apply(query.findPagingList(size), doCount);
	}

	public <T> List<T> apply(PagingList<T> list, boolean doCount) {
		if (doCount) {
			count = list.getTotalRowCount();
		}
		return list.getPage(page).getList();
	}
}
