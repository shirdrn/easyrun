package org.shirdrn.easyrun.component.database;

import java.io.Closeable;

import org.shirdrn.easyrun.common.Resetable;

public interface DBCollection<E> extends Resetable, Closeable, Iterable<E> {

}
