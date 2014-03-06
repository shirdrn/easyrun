package org.shirdrn.easyrun.component.database.engine;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.shirdrn.easyrun.common.Resetable;

/**
 * Builder for setting values for a {@link PreparedStatement} instance.
 * 
 * @author Shirdrn
 */
public interface SQLBuilder extends Resetable {

	SQLBuilder build(String sql) throws SQLException;
	<T> SQLBuilder set(int parameterIndex, T value) throws SQLException;
	<T> SQLBuilder set(T value) throws SQLException;
	PreparedStatement getPreparedStatement();
}
