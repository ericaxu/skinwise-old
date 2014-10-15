package src.models;

import com.avaje.ebean.PagingList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import play.db.ebean.Model;
import src.util.Logger;
import src.util.Util;

import java.sql.SQLException;
import java.util.List;

public class Page {
	private static final String TAG = "Page";

	public int page;
	public int size;
	public int count;

	public Page(int page, int size) {
		this.page = page;
		this.size = size;
		this.count = 0;
	}

	/**
	 * Apply paging on a raw sql query.
	 * The query should exclude the SELECT portion and only start at the FROM section.
	 * <p>
	 * Example: "FROM ingredient table WHERE conditions..."
	 */
	public <I, T> List<T> apply(Model.Finder<I, T> find, String query_from, String table_alias) {
		try {
			count = Util.sqlCount(query_from);
		}
		catch (SQLException e) {
			Logger.fatal(TAG, "SQL Count Query failed", e);
		}
		RawSql sql = RawSqlBuilder.parse("SELECT " + table_alias + ".id " + query_from)
				.columnMapping(table_alias + ".id", "id").create();

		Logger.debug("", sql.getSql().toString());

		return apply(find.setRawSql(sql), false);
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
