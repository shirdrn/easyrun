package org.shirdrn.easyrun.common;

import java.io.IOException;

/**
 * For the purpose of object reuse, a {@link Resetable} object
 * can be used again with creating a new one.
 * 
 * @author Shirdrn
 */
public interface Resetable {

	void reset() throws IOException;
}
