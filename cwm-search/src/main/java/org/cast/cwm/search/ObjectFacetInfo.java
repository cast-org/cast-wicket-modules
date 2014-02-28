/*
 * Copyright 2011-2014 CAST, Inc.
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
