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
package org.cast.cwm.figuration;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

import java.util.LinkedList;
import java.util.List;

/**
 * @author bgoldowsky
 */
public class FigurationUtil {

	public static final JavaScriptReferenceHeaderItem figurationJsHeaderItem = JavaScriptHeaderItem.forReference(
			new UrlResourceReference(Url.parse("https://cdn.jsdelivr.net/figuration/1.3.1/js/figuration.min.js")) {
				@Override
				public List<HeaderItem> getDependencies() {
					List<HeaderItem> dependencies = new LinkedList<>();
					dependencies.add(
							JavaScriptHeaderItem.forReference(
									Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
					return dependencies;
				}
			});


	private static final CssReferenceHeaderItem figurationCSSHeaderItem = CssHeaderItem.forReference(
			new UrlResourceReference(Url.parse("https://cdn.jsdelivr.net/figuration/1.3.1/css/figuration.min.css")));

	/**
	 * Add the header items needed to use the CAST Figuration framework.
	 * @param response the IHeaderResponse that will have items added to it
	 */
	public static void addFigurationHeaderItems(IHeaderResponse response) {
		response.render(new PriorityHeaderItem(StringHeaderItem.forString(
				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">")));
		response.render(new PriorityHeaderItem(StringHeaderItem.forString(
				"<meta http-equiv=\"x-ua-compatible\" content=\"ie=edge\">")));

		response.render(figurationJsHeaderItem);
		response.render(figurationCSSHeaderItem);
	}

}
