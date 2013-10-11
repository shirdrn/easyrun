package org.shirdrn.easyrun.component.database.engine;

import java.io.Closeable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.shirdrn.easyrun.component.database.DBCollection;
import org.shirdrn.easyrun.component.database.DBResult;

public interface DBEngine extends Closeable {

	PreparedStatement prepareStatement(String sql) throws SQLException;
	DBCollection<DBResult> executeQuery(SQLBuilder builder) throws SQLException;
	int executeUpdate(SQLBuilder builder) throws SQLException;
}
