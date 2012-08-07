/*
 * Copyright 2011 CAST, Inc.
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.apache.wicket.util.string.Strings;
import org.cast.cwm.service.UserSpreadsheetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database initializer that will read a CSV spreadsheet of default users and create them in the database.
 * 
 * App Properties expected:
 *  cwm.defaultUserFile = the absolute filename of the CSV file.
 * 
 * @author bgoldowsky
 *
 */
public class CreateDefaultUsers implements IDatabaseInitializer {

	private static final Logger log = LoggerFactory.getLogger(CreateDefaultUsers.class);

	public String getName() {
		return "create default users";
	}

	public boolean isOneTimeOnly() {
		return true;
	}

	public boolean run(Properties appProperties) {
		String userSpreadsheet = appProperties.getProperty("cwm.defaultUserFile");
		if (userSpreadsheet != null) {
			log.debug("Reading {}", userSpreadsheet);
			try {
				FileInputStream file = new FileInputStream(userSpreadsheet);
				UserSpreadsheetReader usr = new UserSpreadsheetReader();
				if (usr.readInput(file)) {
					usr.save();
					return true;
				} else {
					log.error("User spreadsheet contained errors: {}", usr.getGlobalError());
					for (org.cast.cwm.service.UserSpreadsheetReader.PotentialUserSave u : usr.getPotentialUsers()) {
						if (u.getUser()==null || !Strings.isEmpty(u.getError())) {
							log.error("Error line {}: {}", u.getLine(), u.getError());
						}
					}
				}
			} catch (FileNotFoundException e) {
				log.error("cwm.defaultUserFile not found: {}", userSpreadsheet);
			}
		}
		return false;
	}

}
