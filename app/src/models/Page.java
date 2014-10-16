package src.models;

import com.avaje.ebean.PagingList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import play.db.DB;
import play.db.ebean.Model;
import src.util.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

	public static int sqlCount(String query) throws SQLException {
		Connection connection = DB.getConnection();
		Statement statement = connection.createStatement();
		ResultSet result_set = statement.executeQuery("SELECT COUNT(*) FROM (" + query + ")");
		result_set.next();
		int count = result_set.getInt("COUNT(*)");
		result_set.close();
		connection.close();
		return count;
	}

	public <I, T> List<T> apply(Model.Finder<I, T> find, String query) {

		try {
			count = sqlCount(query);
		}
		catch (SQLException e) {
			Logger.fatal(TAG, "SQL Count Query failed", e);
		}

		RawSql sql = RawSqlBuilder.parse(query).create();

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
