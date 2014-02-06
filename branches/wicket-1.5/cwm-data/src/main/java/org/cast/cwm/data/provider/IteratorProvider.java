package org.cast.cwm.data.provider;

import java.io.Serializable;
import java.util.Iterator;

public interface IteratorProvider<T> extends Serializable {

	Iterator<? extends T> getIterator();
}
