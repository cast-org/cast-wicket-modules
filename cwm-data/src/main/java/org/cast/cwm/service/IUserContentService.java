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
package org.cast.cwm.service;

import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.User;
import org.cast.cwm.data.UserContent;

public interface IUserContentService {

	/**
	 * Return a newly-instantiated UserContent object.
	 * This exists so that applications that need to subclass UserContent can override this method.
	 * All service methods will call this, rather than instantiating objects in any other way.
	 * @return an instance of UserContent
	 */
	public UserContent newUserContent (User author, IResponseType dataType);

}
