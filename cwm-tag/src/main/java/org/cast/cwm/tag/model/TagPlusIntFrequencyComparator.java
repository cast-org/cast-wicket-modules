/*
 * Copyright 2011-2013 CAST, Inc.
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
/**
 * TagPlusIntFrequencyComparator.java
 */

package org.cast.cwm.tag.model;

import java.util.Comparator;


/**
 * Sorts tags by frequency, ascending or descending.
 * When frequency is equal, default sort is by name ascending.
 * 
 * @author Tom Green
 */
public class TagPlusIntFrequencyComparator implements Comparator<TagPlusInt> {
   
	public static final Comparator<TagPlusInt> ASCENDING = new TagPlusIntFrequencyComparator(1);
    
	public static final Comparator<TagPlusInt> DESCENDING = new TagPlusIntFrequencyComparator(-1);
    
	private int direction = 1;
    
	public TagPlusIntFrequencyComparator () {}
    
	/**
     * @param dir 
     */
    public TagPlusIntFrequencyComparator (int dir) {
        direction = dir;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
	public int compare (TagPlusInt o1, TagPlusInt o2) {
    	if (o1.getInt() == o2.getInt())
    		return TagPlusIntNameComparator.ASCENDING.compare(o1, o2);
        return direction * Integer.signum(o1.getInt() - o2.getInt());
    }
}
