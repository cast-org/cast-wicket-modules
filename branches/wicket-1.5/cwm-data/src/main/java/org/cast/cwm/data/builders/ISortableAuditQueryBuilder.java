package org.cast.cwm.data.builders;

import org.hibernate.Session;
import org.hibernate.envers.query.AuditQuery;

/**
 * Interface for classes that can build optionally sortable queries over audit table data.
 *
 * @author bgoldowsky
 */
public interface ISortableAuditQueryBuilder extends IAuditQueryBuilder {
	
	public AuditQuery buildSorted(Session session);
	
}