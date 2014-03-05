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
package org.cast.cwm.data.builders;

import java.util.Date;

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
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@Getter
@Setter

// TODO: ResponseType should be a set
public class ResponseCriteriaBuilder implements CriteriaBuilder, OrderingCriteriaBuilder, ISortStateLocator, IDetachable {

	private static final long serialVersionUID = 1L;
	
	private ISortState sortState = new SingleSortState();
	
	private IModel<? extends Prompt> promptModel;
	private IModel<User> userModel;
	private IModel<Period> periodModel;
	private IResponseType responseType;
	private Integer maxResults;
	private Integer sortOrder;
	private Date fromDate;
	private Date toDate;
	
	public ResponseCriteriaBuilder() {
		((SingleSortState) getSortState()).setSort(new SortParam("lastUpdated", true));
	}
	
	public void buildOrdered(Criteria criteria) {
		buildUnordered(criteria);
		SortParam sort = ((SingleSortState) getSortState()).getSort();
		if (sort != null) {
			if (sort.isAscending())
				criteria.addOrder(Order.asc(sort.getProperty()).ignoreCase());
			else
				criteria.addOrder(Order.desc(sort.getProperty()).ignoreCase());
		}
	}

	public void buildUnordered(Criteria criteria) {
		if (promptModel != null && promptModel.getObject() != null)
			criteria.add(Restrictions.eq("prompt", promptModel.getObject()));
		if (userModel != null && userModel.getObject() != null)
			criteria.add(Restrictions.eq("user", userModel.getObject()));
		if (periodModel != null && periodModel.getObject() != null)
			criteria.createAlias("user", "user").createAlias("user.periods", "p" ).add(Restrictions.eq("p.id", periodModel.getObject().getId()));
		if (responseType != null)
			criteria.add(Restrictions.eq("type", responseType));
		if (sortOrder != null)
			criteria.add(Restrictions.eq("sortOrder", sortOrder));
		if (maxResults != null)
			criteria.setMaxResults(maxResults);
		if (fromDate != null && toDate != null && fromDate.before(toDate))
			criteria.add(Restrictions.between("lastUpdated", fromDate, toDate));		
		criteria.add(Restrictions.eq("valid", true));
		criteria.setCacheable(true);
	}

	
	public void build(Criteria criteria) {
		buildOrdered(criteria);
	}	
	
	public void detach() {
		if (promptModel != null)
			promptModel.detach();
		if (userModel != null)
			userModel.detach();
		if (periodModel != null)
			periodModel.detach();
	}
}
