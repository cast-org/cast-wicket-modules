package org.cast.cwm.search;

import lombok.Getter;
import lombok.ToString;

import org.hibernate.search.query.facet.Facet;

/**
 * Specialization of FacetInfo where the facet is based on the values of an enum class.
 * The enum's toString() method is used to find the human-readable name for the facet.
 * 
 * @author bgoldowsky
 *
 */
@ToString
public class EnumFacetInfo extends FacetInfo {
	
	@Getter
	protected Enum<?> valueEnum;
	
	private static final long serialVersionUID = 1L;

	public EnumFacetInfo (Facet facet, Enum<?> valueEnum) {
		super(facet, valueEnum.toString());
		this.valueEnum = valueEnum;
	}
	
	public EnumFacetInfo (String facetName, Enum<?> valueEnum, int count, boolean active) {
		super(facetName, Integer.toString(valueEnum.ordinal()), valueEnum.toString(), count, active);
		this.valueEnum = valueEnum;
	}


}
