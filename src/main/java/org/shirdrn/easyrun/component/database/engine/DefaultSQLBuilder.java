package org.shirdrn.easyrun.component.database.engine;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DefaultSQLBuilder implements SQLBuilder {

	private final DBEngine engine;
	private PreparedStatement ps;
	private boolean built;
	private String sql;
	private int currentIndex;
	private Map<Integer, Object> parameters = new HashMap<>(0);
	
	public DefaultSQLBuilder(DBEngine engine) {
		super();
		this.engine = engine;
	}
	
	@Override
	public SQLBuilder build(String sql) throws SQLException {
		if(!built) {
			this.sql = sql;
			this.ps = engine.prepareStatement(sql);
			built = true;
		} else {
			throw new SQLException("Only building once is permitted!");
		}
		return this;
	}
	
	@Override
	public <T> SQLBuilder set(T value) throws SQLException {
		return set(++currentIndex, value);
	}

	@Override
	public <T> SQLBuilder set(int parameterIndex, T value) throws SQLException {
		if(built) {
			parameters.put(parameterIndex, value);
			switch(value.getClass().getName()) {
				case "java.lang.Integer":
					ps.setInt(parameterIndex, (Integer) value);
					break;
				case "java.lang.Short":
					ps.setShort(parameterIndex, (Short) value);
					break;
				case "java.lang.Boolean":
					ps.setBoolean(parameterIndex, (Boolean) value);
					break;
				case "java.lang.Byte":
					ps.setByte(parameterIndex, (Byte) value);
					break;
				case "java.lang.Long":
					ps.setLong(parameterIndex, (Long) value);
					break;
				case "java.lang.Float":
					ps.setFloat(parameterIndex, (Float) value);
					break;
				case "java.lang.Double":
					ps.setDouble(parameterIndex, (Double) value);
					break;
				case "java.lang.String":
					ps.setString(parameterIndex, (String) value);
					break;
				case "java.lang.Byte[]":
				case "java.lang.byte[]":
					ps.setBytes(parameterIndex, (byte[]) value);
					break;
				case "java.sql.Date":
					ps.setDate(parameterIndex, (java.sql.Date) value);
					break;
				case "java.sql.Array":
					ps.setArray(parameterIndex, (java.sql.Array) value);
					break;
				case "java.sql.Timestamp":
					ps.setTimestamp(parameterIndex, (java.sql.Timestamp) value);
					break;
				case "java.sql.Time":
					ps.setTime(parameterIndex, (java.sql.Time) value);
					break;
				case "java.sql.Ref":
					ps.setRef(parameterIndex, (java.sql.Ref) value);
					break;
				case "java.sql.Clob":
					ps.setClob(parameterIndex, (java.sql.Clob) value);
					break;
				case "java.sql.NClob":
					ps.setNClob(parameterIndex, (java.sql.NClob) value);
					break;
				case "java.sql.Blob":
					ps.setBlob(parameterIndex, (java.sql.Blob) value);
					break;
				case "java.sql.RowId":
					ps.setRowId(parameterIndex, (java.sql.RowId) value);
					break;
				case "java.net.URL":
					ps.setURL(parameterIndex, (java.net.URL) value);
					break;
				case "java.math.BigDecimal":
					ps.setBigDecimal(parameterIndex, (java.math.BigDecimal) value);
					break;
				default:
					ps.setObject(parameterIndex, value);
			} 
		} else {
			throw new SQLException("Must set after invoking build() method!");
		}
		return this;
	}

	@Override
	public PreparedStatement getPreparedStatement() {
		return ps;
	}

	@Override
	public void reset() throws IOException {
		sql = null;
		ps = null;
		currentIndex = 0;
		built = false;
		parameters.clear();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SQL[" + sql + "]:")
		.append(parameters);
		return sb.toString();
	}

}
