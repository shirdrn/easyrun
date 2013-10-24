package org.shirdrn.easyrun.component.database;

import java.math.BigDecimal;
import java.sql.SQLException;

public interface DBResult {

	String getString(int columnIndex) throws SQLException;
    boolean getBoolean(int columnIndex) throws SQLException;
    byte getByte(int columnIndex) throws SQLException;
    short getShort(int columnIndex) throws SQLException;
    int getInt(int columnIndex) throws SQLException;
    long getLong(int columnIndex) throws SQLException;
    float getFloat(int columnIndex) throws SQLException;
    double getDouble(int columnIndex) throws SQLException;
    BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException;
    byte[] getBytes(int columnIndex) throws SQLException;
    java.sql.Date getDate(int columnIndex) throws SQLException;
    java.sql.Time getTime(int columnIndex) throws SQLException;
    java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException;
    java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException;
    java.io.InputStream getBinaryStream(int columnIndex) throws SQLException;


    // Methods for accessing results by column label

    String getString(String columnLabel) throws SQLException;
    boolean getBoolean(String columnLabel) throws SQLException;
    byte getByte(String columnLabel) throws SQLException;
    short getShort(String columnLabel) throws SQLException;
    int getInt(String columnLabel) throws SQLException;
    long getLong(String columnLabel) throws SQLException;
    float getFloat(String columnLabel) throws SQLException;
    double getDouble(String columnLabel) throws SQLException;
    BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException;
    byte[] getBytes(String columnLabel) throws SQLException;
    java.sql.Date getDate(String columnLabel) throws SQLException;
    java.sql.Time getTime(String columnLabel) throws SQLException;
    java.sql.Timestamp getTimestamp(String columnLabel) throws SQLException;
    java.io.InputStream getAsciiStream(String columnLabel) throws SQLException;
    java.io.InputStream getUnicodeStream(String columnLabel) throws SQLException;
    java.io.InputStream getBinaryStream(String columnLabel) throws SQLException;

    // Advanced features:

    Object getObject(int columnIndex) throws SQLException;
    Object getObject(String columnLabel) throws SQLException;

}
