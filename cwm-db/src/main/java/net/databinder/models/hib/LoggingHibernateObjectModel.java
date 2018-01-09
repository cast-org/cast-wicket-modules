/*
 * Copyright 2011-2018 CAST, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class LoggingHibernateObjectModel<T> extends HibernateObjectModel<T> {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(LoggingHibernateObjectModel.class);

	public LoggingHibernateObjectModel() {
		super();
	}

	public LoggingHibernateObjectModel(Class objectClass,
			ICriteriaBuilder criteriaBuilder) {
		super(objectClass, criteriaBuilder);
	}

	public LoggingHibernateObjectModel(Class objectClass, Serializable entityId) {
		super(objectClass, entityId);
	}

	public LoggingHibernateObjectModel(Class objectClass) {
		super(objectClass);
	}

	public LoggingHibernateObjectModel(QueryBuilder queryBuilder) {
		super(queryBuilder);
	}

	public LoggingHibernateObjectModel(String queryString,
			QueryBinder queryBinder) {
		super(queryString, queryBinder);
	}

	public LoggingHibernateObjectModel(T persistentObject) {
		super(persistentObject);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		log.debug("Attached object for id={}: {}", getObjectId(), this);
        // Thread.dumpStack();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		log.debug("Detached model for id={}", getObjectId());
	}

}
