package src.models.util;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import play.db.DB;
import src.util.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SelectQuery {
	private static final String TAG = "SelectQuery";

	private StringBuilder buffer = new StringBuilder();
	private boolean select = false;
	private boolean from = false;
	private boolean where = false;

	public SelectQuery select(String input) {
		if (from || where) {
			throw new RuntimeException("Query error!");
		}
		if (!select) {
			select = true;
			buffer.append(" SELECT ");
		}
		else {
			buffer.append(", ");
		}
		buffer.append(input);
		return this;
	}

	public SelectQuery from(String input) {
		if (!select || where) {
			throw new RuntimeException("Query error!");
		}
		if (!from) {
			from = true;
			buffer.append(" FROM ");
		}
		else {
			buffer.append(", ");
		}
		buffer.append(input);
		return this;
	}

	public SelectQuery where(String input) {
		if (!select || !from) {
			throw new RuntimeException("Query error!");
		}
		if (!where) {
			where = true;
			buffer.append(" WHERE ");
		}
		else {
			buffer.append(" AND ");
		}
		buffer.append(input);
		return this;
	}

	public SelectQuery other(String input) {
		buffer.append(" ").append(input);
		return this;
	}

	public String get() {
		return buffer.toString();
	}

	public TLongList execute() {
		TLongList result = new TLongArrayList();
		try {
			Connection connection = DB.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(get());
			while (resultSet.next()) {
				result.add(resultSet.getLong("id"));
			}
			resultSet.close();
			connection.close();
		}
		catch (SQLException e) {
			Logger.error(TAG, e);
		}
		return result;
	}
}
