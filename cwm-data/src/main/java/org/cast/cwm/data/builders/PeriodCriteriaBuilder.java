/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.data.builders;

import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.ICriteriaBuilder;
import org.cast.cwm.data.Site;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * A simple ICriteriaBuilder that locates a Period.
 * 
 * @author lmccormack
 *
 */
@Getter
@Setter
public class PeriodCriteriaBuilder implements ICriteriaBuilder {
	
	private String name = null;
	private Site site = null;
	private Integer maxResults = null;	

	public PeriodCriteriaBuilder() {
		
	}
	
	@Override
	public void buildUnordered(Criteria criteria) {
		if (name != null)
			criteria.add(Restrictions.eq("name", name));
		if (site != null)
			criteria.add(Restrictions.eq("site", site));
		if (maxResults != null)
			criteria.setMaxResults(maxResults);
		criteria.setCacheable(true);
	}

	@Override
	public void buildOrdered(Criteria criteria) {
		buildUnordered(criteria);
		criteria.addOrder(Order.asc("name"));
	}

}