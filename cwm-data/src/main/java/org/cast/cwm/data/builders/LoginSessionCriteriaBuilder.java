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
/**
 * 
 */
package org.cast.cwm.data.builders;

import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.CriteriaBuilder;
import net.databinder.models.hib.OrderingCriteriaBuilder;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class LoginSessionCriteriaBuilder implements CriteriaBuilder, OrderingCriteriaBuilder, ISortStateLocator<String> {

	@Getter @Setter
	private ISortState<String> sortState;
	
	@Getter @Setter
	private boolean openOnly = true;
	
	@Getter @Setter
	private Long userId = null;
	
	private static final long serialVersionUID = 1L;

	public LoginSessionCriteriaBuilder() {
		SingleSortState<String> sort = new SingleSortState<String>();
		sort.setSort(new SortParam<String>("startTime", true));
		sortState = sort;
	}

	@Override
	public void build(Criteria criteria) {
		buildOrdered(criteria);
	}

	@Override
	public void buildOrdered(Criteria criteria) {
		buildUnordered(criteria);
		SortParam<String> sort = ((SingleSortState<String>) getSortState()).getSort();
		if (sort != null) {
			if (sort.isAscending())
				criteria.addOrder(Order.asc(sort.getProperty()).ignoreCase());
			else
				criteria.addOrder(Order.desc(sort.getProperty()).ignoreCase());
		}
	}

	@Override
	public void buildUnordered(Criteria criteria) {
		if (openOnly)
			criteria.add(Restrictions.isNull("endTime"));

		criteria.createAlias("user", "user"); // Must be joined for sort on username to work.
		if (userId != null)
			criteria.add(Restrictions.eq("user.id", userId));
		
		criteria.setCacheable(true);			
	}
	
}