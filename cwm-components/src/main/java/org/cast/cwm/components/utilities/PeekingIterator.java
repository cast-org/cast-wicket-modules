/*
 * Copyright 2011-2019 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.cwm.components.utilities;

import java.util.Iterator;

/**
 * PeekingIterator<E>
 * 
 * An iterator decorator providing a one-step lookahead
 * 
 * @author droby
 *
 * 
 *  @see java.util.Iterator
 *  
 *  TODO:  This is available in guava, and we might want to use that instead.
 */
public class PeekingIterator<E> {

  private final Iterator<? extends E> iterator;
  private boolean hasPeeked;
  private E peekedElement;

  public PeekingIterator(Iterator<? extends E> iterator) {
	  if (iterator == null)
		  throw new IllegalArgumentException("Null iterator");
    this.iterator = iterator;
  }

  public boolean hasNext() {
    return hasPeeked || iterator.hasNext();
  }

  public E next() {
    if (!hasPeeked) {
      return iterator.next();
    }
    E result = peekedElement;
    hasPeeked = false;
    peekedElement = null;
    return result;
  }

  public void remove() {
	  throw new UnsupportedOperationException();
  }

  public E peek() {
    if (!hasPeeked) {
      peekedElement = iterator.next();
      hasPeeked = true;
    }
    return peekedElement;
  }
}
