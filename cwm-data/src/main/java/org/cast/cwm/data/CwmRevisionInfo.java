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
package org.cast.cwm.data;

import javax.persistence.Entity;

import lombok.Getter;

import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.cast.cwm.CwmSession;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionListener;

/**
 * Custom RevisionEntity for audit logs.
 * Adds an indication of the logged in user who caused the revision (if any).
 * 
 * @author bgoldowsky
 *
 */
@Entity
@RevisionEntity(value=CwmRevisionInfo.CwmRevisionListener.class)
public class CwmRevisionInfo extends DefaultRevisionEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * Reference to the login session of the user who caused this change.
	 */
	@Getter
	private Long loginSessionId;
	
	public static class CwmRevisionListener implements RevisionListener {
		
	    public void newRevision(Object revisionEntity) {
	    	// Unlike Session.get(), this will never create a Session if one doesn't already exist.
	    	Session session = ThreadContext.getSession();
	    	if (session instanceof CwmSession) {
	    		((CwmRevisionInfo) revisionEntity).loginSessionId = ((CwmSession)session).getLoginSessionId();
	    	}
	    }
	}
}
