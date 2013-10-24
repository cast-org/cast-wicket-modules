package org.cast.cwm.data.builders;

import org.cast.cwm.data.UserContent;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

/**
 * Builds queries over the audit data for UserContent.
 * 
 * @author bgoldowsky
 *
 */
public class UserContentAuditQueryBuilder implements ISortableAuditQueryBuilder {

	private static final long serialVersionUID = 1L;

	@Override
	public AuditQuery build(Session session) {
		AuditReader auditReader = AuditReaderFactory.get(session);
		return auditReader.createQuery().forRevisionsOfEntity(UserContent.class, false, true);
	}
	
	@Override
	public AuditQuery buildSorted (Session session) {
		AuditQuery query = build(session);
		query.addOrder(AuditEntity.revisionProperty("timestamp").desc());
		return query;
	}
	
}