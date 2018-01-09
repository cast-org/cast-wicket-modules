/*
 * Copyright 2011-2018 CAST, Inc.
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
