/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.search;

import java.io.Serializable;

import lombok.Data;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.search.query.facet.Facet;

/**
 * Holds various information about a facet value, used in displaying the search box
 * and building clickable links.  Unlike the Facet object itself, this is serializable
 * and not connected to any Hibernate session.
 */
@Data
public class FacetInfo implements Serializable {
	// Which facet group is this part of?  Eg, grade level or subject area
	private final String facetName;
	
	// Value of the facet, eg "ENGLISH"
	private final String value;
	
	// Human-readable version of the name, eg "English Language Arts"
	private String valueName;
	
	// Count of documents matching this facet, from search
	private IModel<Integer> mCount;
	
	// True if this value is currently part of the search
	private Boolean active;
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Construct by specifying all the fields.
	 */
	public FacetInfo (String facetName, String value, String valueName, IModel<Integer> mCount, boolean active) {
		this.facetName = facetName;
		this.value = value;
		this.valueName = valueName;
		this.mCount = mCount;
		this.active = active;		
	}
	
	/**
	 * Construct with a literal count, rather than a model.
	 */
	public FacetInfo (String facetName, String value, String valueName, int count, boolean active) {
		this(facetName, value, valueName, Model.of(count), active);
	}
	
	/**
	 * Construct from a hibernate-search Facet, plus a human-readable label.
	 * @param facet
	 * @param valueName
	 */
	public FacetInfo (Facet facet, String valueName) {
		this.value = facet.getValue();
		this.mCount = Model.of(facet.getCount());
		this.facetName = facet.getFacetingName();
		this.valueName = valueName;
		this.active = false;
	}
	
	public Integer getCount() {
		return mCount.getObject();
	}
	
	/**
	 * Return the count of matches as a string.
	 * This is a convenience getter for use in PropertyModels.
	 * @return a string representation of the match count.
	 */
	public String getCountAsString() {
		return Integer.toString(mCount.getObject());
	}
	
	public void detach() {
		if (mCount != null)
			mCount.detach();
	}

}