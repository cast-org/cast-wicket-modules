package org.cast.cwm.search;

import java.io.Serializable;

import lombok.Data;

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
	private int count;
	
	// True if this value is currently part of the search
	private Boolean active;
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Construct by specifying all the fields.
	 */
	public FacetInfo (String facetName, String value, String valueName, int count, boolean active) {
		this.facetName = facetName;
		this.value = value;
		this.valueName = valueName;
		this.count = count;
		this.active = active;
	}
	
	/**
	 * Construct from a hibernate-search Facet, plus a human-readable label.
	 * @param facet
	 * @param valueName
	 */
	public FacetInfo (Facet facet, String valueName) {
		value = facet.getValue();
		count = facet.getCount();
		facetName = facet.getFacetingName();
		this.valueName = valueName;
		active = false;
	}
	
	/**
	 * Return the count of matches as a string.
	 * This is a convenience getter for use in PropertyModels.
	 * @return
	 */
	public String getCountAsString() {
		return Integer.toString(count);
	}
}