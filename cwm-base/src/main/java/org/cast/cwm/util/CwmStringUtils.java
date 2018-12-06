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
package org.cast.cwm.util;

/**
 * Holds a few utililty methods for string manipulation.
 * 
 * @author bgoldowsky
 *
 */
public class CwmStringUtils {

	/**
	 * Replace any string of whitespace characters with a single space,
	 * and remove whitespace from the beginning and end of the string.
	 * 
	 * @return the new string.
	 */
	public static String normalizeWhitespace(String orig) {
		return orig.replaceAll("\\s+", " ").trim();
	}
	
}
