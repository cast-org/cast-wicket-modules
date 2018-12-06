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
package org.cast.cwm.service;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Site;

import java.io.InputStream;
import java.util.List;

/**
 * An object capable of digesting a stream of input data (usually a CSV file) that represents users
 * that could be created, and creating those users in the database.
 *
 * This is accomplished as a two-stage operation.  In the first stage, the input data is read and a list of
 * "potential users" is created and stored in memory.  The list (possibly including global or user-specific errors)
 * can be fetched and displayed to the user.
 *
 * If there are no errors, the potential users can then be created in the database.
 *
 * @author bgoldowsky
 */
public interface ISpreadsheetReader {

	/**
	 * Read spreadsheet of user information and internally remember results.
	 * Returns true if reading and parsing of input stream was successful and a save could be requested.
	 *
	 * This method does NOT modify the datastore.
	 * Results can be accessed by calling {@link #getPotentialUsers()}.
	 *
	 * @param stream the input stream of CSV data
	 * @return true if no errors encountered.
	 */
	boolean readInput (InputStream stream);

	/**
	 * Save remembered "potential users" to the datastore.
	 * This method should only be called after readInput has been called and completed successfully.
	 *
	 * @param triggerComponent the component that triggered the save (for logging)
	 */
	void save(Component triggerComponent);

	IModel<Site> getDefaultSite();

	ISpreadsheetReader setDefaultSite(IModel<Site> mSite);

	/**
	 * Return error description if problems were encountered reading or parsing the input stream.
	 *
	 * @return string describing the error condition, or null if no error.
	 */
	String getGlobalError();

	List<UserSpreadsheetReader.PotentialUserSave> getPotentialUsers();

}
