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
package org.cast.cwm.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lynnmccormack
 * Stream the contents of a URL post to a string.
 */
public class UrlStreamedToString {

	protected URL url;
	private static final Logger log = LoggerFactory
			.getLogger(UrlStreamedToString.class);

	public UrlStreamedToString(URL url) {
		this.url = url;
	}

	/**
	 * @return the string of data streamed from the URL post
	 */
	public String getPostString() {
		StringBuffer stringBuffer = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				stringBuffer.append(inputLine);
			in.close();
		} catch (IOException e) {
			log.error("There is a problem opening the url {}", url);
			e.printStackTrace();
		}

		return stringBuffer.toString();
	}

}
