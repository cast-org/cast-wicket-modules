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
package org.cast.cwm.data.init;

import org.cast.cwm.IAppConfiguration;

public interface IDatabaseInitializer {
	
	/** A unique name for this initializer.  
	 * This is stored into the DB to indicate that the initializer has been run, used in log messages, etc.
	 * It must be unique.
	 * @return the official name
	 */
	public String getName();
	
	/** Should this initializer be run only once ever?
	 * If false, it will be run every time the application starts up.
	 * @return
	 */
	public boolean isOneTimeOnly();
	
	/** Called to run the initializer.
	 * It will be run with an open Hibernate session and transaction.
	 * Return true if the run should be recorded in the database.
	 * By convention, it should return true if any database changes were made.
	 * 
	 * @param appProperties the application properties object, which may provide configuration information to the initializer.
	 * @returns true if a database change was made
	 */
	public boolean run(IAppConfiguration appProperties);

}
