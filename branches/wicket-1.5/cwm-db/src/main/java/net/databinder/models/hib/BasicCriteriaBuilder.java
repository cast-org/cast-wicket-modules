/*
 * Copyright 2011-2013 CAST, Inc.
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
 * A simple implementation of a CriteriaBuilder that applies some given Restrictions.
 * 
 * Example usage:
 * 
 * {@code new BasicCriteriaBuilder(Restrictions.eq("name", "bob")) }
 * 
 * @author bgoldowsky
 *
 */
public class BasicCriteriaBuilder implements CriteriaBuilder {

	private static final long serialVersionUID = 1L;
	
	private Criterion[] requestedCriteria;
	
	private Order requestedOrder = null;
	
	/**
	 * Construct a simple CriteriaBuidler from a list of criteria
	 * @param criteria
	 */
	public BasicCriteriaBuilder(Criterion... criteria) {
		this.requestedCriteria = criteria;
	}
	
	/**
	 * Construct from a list of criteria and an ordering instruction. 
	 */
	public BasicCriteriaBuilder(Order order, Criterion... criteria) {
		this.requestedOrder = order;
		this.requestedCriteria = criteria;
	}

	public void build(Criteria query) {
		for (int i=0; i<requestedCriteria.length; i++)
			query.add(requestedCriteria[i]);
		if (requestedOrder != null)
			query.addOrder(requestedOrder);
	}

}
