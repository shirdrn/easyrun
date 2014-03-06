package org.shirdrn.easyrun.component.database;

import java.io.Closeable;

import org.shirdrn.easyrun.common.Resetable;

/**
 * Convert a  database {@link ResultSet} object to a iterable collection. 
 * Do like that we can simplify the operation and operate a {@link DBCollection}
 * object like a regular collection object.</br>
 * 
 * @author Shirdrn
 *
 * @param <E>
 */
public interface DBCollection<E> extends Resetable, Closeable, Iterable<E> {

}
