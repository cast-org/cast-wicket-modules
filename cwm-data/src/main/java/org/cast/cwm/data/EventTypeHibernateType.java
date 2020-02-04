/*
 * Copyright 2011-2020 CAST, Inc.
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

import com.google.inject.Inject;
import org.apache.wicket.injection.Injector;
import org.cast.cwm.IEventType;
import org.cast.cwm.service.IEventService;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Hibernate UserType for the IEventType interface (generally, but not necessarily, implemented as an enum).
 * Uses the name() method of IEventType as the string value to store in the database,
 * and IEventService's getEventType(String) method to look up the values when they are read out.
 */
public class EventTypeHibernateType implements UserType, Serializable {

	@Inject
	private IEventService eventService;

	public EventTypeHibernateType() {
		super();
		Injector.get().inject(this);
	}
	
	@Override
	public int[] sqlTypes() {
		return new int[] {		
				StringType.INSTANCE.sqlType()
		};
	}

	@Override
	public Class<?> returnedClass() {
		return IEventType.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		return ( x == y ) || ( x != null && x.equals( y ) );
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		assert names.length == 1;
		String typeName = rs.getString(names[0]);
		if (typeName == null)
			return null;
		return eventService.getEventType(typeName);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index,  SessionImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
			StringType.INSTANCE.set(st, null, index, session);
		} else {
			IEventType type = (IEventType) value;
			StringType.INSTANCE.set(st, type.name(), index, session);
		}
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (IEventType)value;
	}

	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

}
