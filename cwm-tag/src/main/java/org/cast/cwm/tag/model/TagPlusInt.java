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
package org.cast.cwm.tag.model;

import java.io.Serializable;


public class TagPlusInt implements Serializable {

	private static final long serialVersionUID = 1L;

	private Tag tag;

	private int intVal;

	public TagPlusInt (Tag t, int i) {
		tag = t;
		intVal = i;
	}

	/**
	 * Hibernate requires a Long constructor for counts
	 * @param t
	 * @param i
	 */
	public TagPlusInt (Tag t, Long i) {
		this(t, i.intValue());
	}

	public int getInt () {
		return intVal;
	}

	public void setInt (int i) {
		intVal = i;
	}

	public Tag getTag () {
		return tag;
	}

	public void setTag (Tag tag) {
		this.tag = tag;
	}

}
