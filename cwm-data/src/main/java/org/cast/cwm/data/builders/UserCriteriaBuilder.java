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
package org.cast.cwm.data.builders;

import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.ICriteriaBuilder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
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
public class UserCriteriaBuilder implements ICriteriaBuilder, ISortStateLocator<String>, IDetachable {

	private Role role = null;
	private boolean getAllUsers = true; // should users with valid=false be included?
	private boolean permissionedOnly = false; // should users with no research permissions be excluded?
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String subjectId;
	private IModel<? extends Period> period;
	private IModel<? extends Collection<Site>> sites;
	private ISortState<String> sortState = new SingleSortState<String>();
	private boolean cacheResults = true;
	
	public UserCriteriaBuilder() {
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
		} else {
			// by default, sort to lastname, firstname
			criteria.addOrder(Order.asc("lastName").ignoreCase());
			criteria.addOrder(Order.asc("firstName").ignoreCase());
		}
	}
	
	@Override
	public void buildUnordered(Criteria criteria) {
		if (role != null)
			criteria.add(Restrictions.eq("role", role));
		if (!getAllUsers)
			criteria.add(Restrictions.eq("valid", true));
		if (permissionedOnly)
			criteria.add(Restrictions.eq("permission", true));
		if (username != null)
			criteria.add(Restrictions.eq("username", username).ignoreCase());
		if (email != null)
			criteria.add(Restrictions.eq("email", email).ignoreCase());
		if (subjectId != null)
			criteria.add(Restrictions.eq("subjectId", subjectId).ignoreCase());
		if (firstName != null)
			criteria.add(Restrictions.eq("firstName", firstName).ignoreCase());
		if (lastName != null)
			criteria.add(Restrictions.eq("lastName", lastName).ignoreCase());
		if (period != null && period.getObject() != null)
			criteria.createAlias("periods", "p").add(Restrictions.eq("p.id", period.getObject().getId()));
		
		if (sites != null && sites.getObject() != null) {
			// Restricted by site membership
			criteria.createAlias("periods", "p");
			if (!sites.getObject().isEmpty())
				criteria.add(Restrictions.in("p.site", sites.getObject().toArray()));
			else
				criteria.add(Restrictions.idEq(0L));  // no sites selected, return no users
			// TODO should allow queries for users with no site at all, like @EventLog .
		}

		if (cacheResults)
			criteria.setCacheable(true);	
	}
	
	@Override
	public void detach() {
		if (period != null)
			period.detach();
	}
}
