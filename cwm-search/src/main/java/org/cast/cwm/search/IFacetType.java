package org.cast.cwm.search;

/**
 * Class that can be used as a facet for searching.
 * Objects of this class have to have an id, and a human-readable name.
 * 
 */
public interface IFacetType {

	public Long getId();
	
	public String getName();
	
}
