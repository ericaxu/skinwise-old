package src.models;

import com.avaje.ebean.Query;
import play.db.ebean.Model;

public class Page {
	public int page;
	public int size;

	public Page(int page, int size) {
		this.page = page;
		this.size = size;
	}

	public <I, T extends Model> Query<T> apply(Model.Finder<I, T> find) {
		return apply(find.query());
	}

	public <T> Query<T> apply(Query<T> query) {
		return query
				.setMaxRows(size)
				.setFirstRow(page * size);
	}
}
