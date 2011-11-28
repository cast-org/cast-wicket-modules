package org.cast.cwm.search;

import lombok.Getter;
import lombok.ToString;

import org.apache.wicket.model.IModel;
import org.hibernate.search.query.facet.Facet;

/**
 * Specialization of FacetInfo where the facet is based on an IFacetType object.
 * 
 * @author bgoldowsky
 *
 */
@ToString
public class ObjectFacetInfo extends FacetInfo {
	
	@Getter
	protected IModel<? extends IFacetType> mValue;
	
	private static final long serialVersionUID = 1L;

	public ObjectFacetInfo (Facet facet, IModel<? extends IFacetType> mValue) {
		super(facet, mValue.getObject().getName());
		this.mValue = mValue;
	}
	
	public ObjectFacetInfo (String facetName, IModel<? extends IFacetType> mValue, int count, boolean active) {
		super(facetName, Long.toString(mValue.getObject().getId()), mValue.getObject().getName(), count, active);
		this.mValue = mValue;
	}



}
