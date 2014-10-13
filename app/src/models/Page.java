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

	public <T> List<T> apply(ExpressionList<T> query) {
		PagingList<T> list = query.findPagingList(size);
		count = list.getTotalRowCount();
		return list.getPage(page).getList();
	}

	public <T> List<T> apply(Query<T> query) {
		PagingList<T> list = query.findPagingList(size);
		count = list.getTotalRowCount();
		return list.getPage(page).getList();
	}
}
