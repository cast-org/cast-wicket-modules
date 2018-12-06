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
package org.cast.cwm.components.head;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.Response;

import java.util.Collections;

/**
 * Inserts the Google "Universal Analytics" tracking code into the page header.
 * The property-specific tracking ID and Google Analytics account ID must be supplied.
 * See: https://www.google.com/analytics
 *  
 * @author bgoldowsky
 *
 */
public class GoogleAnalyticsHeaderItem extends HeaderItem {
	
	private static final String format = "<script>\n"
			+ "  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n"
			+ "  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n"
			+ "  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n"
			+ "  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');\n"
			+ "\n"
			+ "  ga('create', '%s', '%s');\n"
			+ "  ga('send', 'pageview');\n"
			+ "</script>\n";

	private String trackingId;
	private String accountId;
	
	private static final long serialVersionUID = 1L;

	public GoogleAnalyticsHeaderItem(String trackingId, String accountId) {
		super();
		this.trackingId = trackingId;
		this.accountId = accountId;
	}

	@Override
	public Iterable<?> getRenderTokens() {
		return Collections.singleton("GoogleAnalyticsHeaderItem");
	}

	@Override
	public void render(Response response) {
		response.write(String.format(format, trackingId, accountId));
		
	}
	
}
