package org.shirdrn.easyrun.common;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface LoopedSQLMaker {

	String makeParentSQL();
	String[] makeChildSQLs(ResultSet rs) throws SQLException;
	
}
