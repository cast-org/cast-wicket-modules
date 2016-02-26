/*
 * Copyright 2011-2016 CAST, Inc.
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
package org.cast.cwm.data.util;

import java.util.List;

import org.apache.wicket.core.util.objects.checker.IObjectChecker;
import org.cast.cwm.data.PersistedObject;

/**
 * Check and complain if there are any PersistedObjects being serialized.
 * {@link PersistedObject} is cwm-data's base class for database objects.
 * If they have an ID assigned, then they are the Java representation of actual 
 * rows in the database.  As such, they shouldn't be serialized as part of the session;
 * they should either be transient or encapsulated in detachable models (and those models
 * should be properly detached!).
 *  
 * This is designed to be called by the serializer.  
 * See https://cwiki.apache.org/confluence/display/WICKET/Serialization+Checker
 * 
 * @author bgoldowsky
 * @author Nick Pratt on the Wicket Wiki
 */
public class PersistedObjectsNotAllowedChecker implements IObjectChecker {

	@Override
	public Result check(Object object) {
		if (object instanceof PersistedObject) {
			PersistedObject target = (PersistedObject) object;
			if (!target.isTransient()) {
				return new Result(Result.Status.FAILURE,
                        "Stored PersistedObjects are not allowed: " + target.getClass().getName() 
                        + ":" + target.getId() + " - " + target.toString());
			}
		}
		return Result.SUCCESS;
	}

	@Override
	public List<Class<?>> getExclusions() {
		return null;
	}

}
