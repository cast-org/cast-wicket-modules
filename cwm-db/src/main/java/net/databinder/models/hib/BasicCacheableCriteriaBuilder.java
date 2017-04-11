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
package net.databinder.models.hib;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

/**
 * Simple ICriteriaBuilder implementation that sets the cacheable flag.
 * Other than caching, identical to {@link BasicCriteriaBuilder}.
 * 
 * @author bgoldowsky
 *
 */
public class BasicCacheableCriteriaBuilder extends BasicCriteriaBuilder {

	private static final long serialVersionUID = 1L;

	public BasicCacheableCriteriaBuilder(Criterion... criteria) {
		super(criteria);
	}

	public BasicCacheableCriteriaBuilder(Order order, Criterion... criteria) {
		super(order, criteria);
	}

	@Override
	public void buildUnordered(Criteria criteria) {
		super.buildUnordered(criteria);
		criteria.setCacheable(true);
	}

}
