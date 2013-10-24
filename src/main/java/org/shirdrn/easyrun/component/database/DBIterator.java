package org.shirdrn.easyrun.component.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class DBIterator implements Iterator<DBResult> {

	private final ResultSet rs;
	
	public DBIterator(ResultSet rs) {
		this.rs = rs;
	}
	
	@Override
	public boolean hasNext() {
		try {
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public DBResult next() {
		DBResult result = new DefaultDBResult(rs);
		return result;
	}

	@Override
	public void remove() {
		Exception ex = new UnsupportedOperationException("Prohibit deletion operation when iteration.");
		throw new RuntimeException(ex);		
	}

}
