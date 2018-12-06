/*
 * Copyright 2011-2019 CAST, Inc.
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
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.data.UserContent;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

/**
 * Builds queries over the audit data for UserContent.
 * 
 * @author bgoldowsky
 *
 */
public class UserContentAuditQueryBuilder implements ISortableAuditQueryBuilder {

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter
	protected IModel<Date> mFromDate;

	@Getter @Setter
	protected IModel<Date> mToDate;

	@Getter @Setter
	IModel<List<User>> mUsers;
	
	@Override
	public AuditQuery build(Session session) {
		AuditReader auditReader = AuditReaderFactory.get(session);
		AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(UserContent.class, false, true);

		if (mUsers != null) {
			List<User> users = mUsers.getObject();
			if (users != null) {
				if (users.isEmpty()) {
					query.add(AuditEntity.property("user_id").eq(0L)); // no users at all
				} else {
					Long[] ids = new Long[users.size()];
					for (int i=0; i< users.size(); i++)
						ids[i] = users.get(i).getId();
					query.add(AuditEntity.property("user_id").in(ids));
				}
			}
		}
		
		if (mFromDate != null && mFromDate.getObject() != null)
			query.add(AuditEntity.revisionProperty("timestamp").ge(midnightStart(mFromDate.getObject()).getTime()));
		if (mToDate != null && mToDate.getObject() != null)
			query.add(AuditEntity.revisionProperty("timestamp").le(midnightEnd(mToDate.getObject()).getTime()));
		return query;
	}
	
	@Override
	public AuditQuery buildSorted (Session session) {
		AuditQuery query = build(session);
		query.addOrder(AuditEntity.revisionProperty("timestamp").desc());
		return query;
	}
	
	protected Date midnightEnd(Date olddate) {
		DateTime dateTime = new DateTime(olddate);
		DateTime adjustedDateTime
			= dateTime
				.plusDays(1)
				.withTimeAtStartOfDay();
		return adjustedDateTime.toDate();
	}

	protected Date midnightStart(Date olddate) {
		DateTime dateTime = new DateTime(olddate);
		DateTime adjustedDateTime
			= dateTime
				.withTimeAtStartOfDay();
		return adjustedDateTime.toDate();
	}

}