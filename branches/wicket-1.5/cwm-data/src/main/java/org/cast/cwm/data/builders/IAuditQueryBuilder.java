package org.cast.cwm.data.builders;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.envers.query.AuditQuery;

/**
 * Interface for a class that can build a query over Hibernate Envers audit tables.
 * 
 * @author bgoldowsky
 *
 */
public interface IAuditQueryBuilder extends Serializable {
	
	public AuditQuery build(Session session);
	
}