package org.shirdrn.easyrun.component.database;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class DefaultDBCollection implements DBCollection<DBResult> {

	private final ResultSet rs;
	private final Iterator<DBResult> iterator;
	
	public DefaultDBCollection(ResultSet rs) {
		super();
		this.rs = rs;
		this.iterator = new DBIterator(rs);
	}
	
	@Override
	public Iterator<DBResult> iterator() {
		return iterator;
	}

	@Override
	public void close() throws IOException {
		try {
			if(rs != null && !rs.isClosed()) {
				rs.close();
			}
		} catch (SQLException e) {
			throw new IOException(e);
		}		
	}

	@Override
	public void reset() throws IOException {
		try {
			rs.first();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
		

}
