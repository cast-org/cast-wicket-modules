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
 * Sorts TagPlusInt according to the tag names.
 */
public class TagPlusIntNameComparator implements Comparator<TagPlusInt> {
   
	public static final Comparator<TagPlusInt> ASCENDING = new TagPlusIntNameComparator(1);
    
	public static final Comparator<TagPlusInt> DESCENDING = new TagPlusIntNameComparator(-1);
    
	private int direction = 1;
    
	public TagPlusIntNameComparator () {}
    
	/**
     * @param dir 
     */
    public TagPlusIntNameComparator (int dir) {
        direction = dir;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare (TagPlusInt o1, TagPlusInt o2) {
        return direction * o1.getTag().getName().compareToIgnoreCase(o2.getTag().getName());
    }
}
