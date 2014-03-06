package org.shirdrn.easyrun.component.database.engine;

import java.io.Closeable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.shirdrn.easyrun.component.database.DBCollection;
import org.shirdrn.easyrun.component.database.DBResult;

/**
 * Database engine for manipulating database, including CRUD operations.
 * Execute a SQL query, we should create a {@link SQLBuilder} object.
 * 
 * @author Shirdrn
 *
 */
public interface DBEngine extends Closeable {

	PreparedStatement prepareStatement(String sql) throws SQLException;
	DBCollection<DBResult> executeQuery(SQLBuilder builder) throws SQLException;
	int executeUpdate(SQLBuilder builder) throws SQLException;
}
