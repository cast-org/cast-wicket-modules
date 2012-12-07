/*
 * Copyright 2011 CAST, Inc.
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
import net.databinder.models.hib.CriteriaBuilder;
import net.databinder.models.hib.OrderingCriteriaBuilder;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
/**
 * <p>
 * An object used to query for {@link User} object(s).
 * </p>
 * <p>
 * NOTE: By default, this query will return both
 * valid and invalid users.  To return only active, valid users
 * {@link #setGetAllUsers(boolean)} to false.
 * </p>
 * 
 * @author jbrookover
 *
 */
@Getter
@Setter
public class UserCriteriaBuilder implements CriteriaBuilder, OrderingCriteriaBuilder, ISortStateLocator, IDetachable {

	private static final long serialVersionUID = 1L;
	private Role role = null;
	private boolean getAllUsers = true;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String subjectId;
	private IModel<Period> period;
	private ISortState sortState = new SingleSortState();
	private boolean cacheResults = true;
	
	public UserCriteriaBuilder() {
	}
	
	public void build(Criteria criteria) {
		buildOrdered(criteria);
	}
	
	public void buildOrdered(Criteria criteria) {
		buildUnordered(criteria);
		SortParam sort = ((SingleSortState) getSortState()).getSort();
		if (sort != null) {
			if (sort.isAscending())
				criteria.addOrder(Order.asc(sort.getProperty()).ignoreCase());
			else
				criteria.addOrder(Order.desc(sort.getProperty()).ignoreCase());
		} else {
			// by default, sort to lastname, firstname
			criteria.addOrder(Order.asc("lastName").ignoreCase());
			criteria.addOrder(Order.asc("firstName").ignoreCase());
		}
	}
	
	public void buildUnordered(Criteria criteria) {
		if (role != null)
			criteria.add(Restrictions.eq("role", role));
		if (!getAllUsers)
			criteria.add(Restrictions.eq("valid", true));
		if (username != null)
			criteria.add(Restrictions.eq("username", username).ignoreCase());
		if (email != null)
			criteria.add(Restrictions.eq("email", email).ignoreCase());
		if (subjectId != null)
			criteria.add(Restrictions.eq("subjectId", subjectId).ignoreCase());
		if (period != null && period.getObject() != null)
			criteria.createAlias("periods", "p").add(Restrictions.eq("p.id", period.getObject().getId()));
		if (firstName != null)
			criteria.add(Restrictions.eq("firstName", firstName).ignoreCase());
		if (lastName != null)
			criteria.add(Restrictions.eq("lastName", lastName).ignoreCase());
		if (cacheResults)
			criteria.setCacheable(true);	
	}
	
	public void detach() {
		if (period != null)
			period.detach();
	}
}
