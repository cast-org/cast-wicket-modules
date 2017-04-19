/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.figuration.service;

import lombok.Getter;
import org.apache.wicket.Application;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of IFigurationService.
 *
 * @author bgoldowsky
 */
public class FigurationService implements IFigurationService {

	private static final String FIGURATION_DEFAULT_VERSION = "2.0.0";

	@Getter
	private final String figurationVersion;

	/**
	 * Default constructor.
	 * Will set the library to use the default Figuration version.
	 * @see #FIGURATION_DEFAULT_VERSION
	 */
	public FigurationService () {
		this(FIGURATION_DEFAULT_VERSION);
	}

	/**
	 * Construct with a specific version of Figuration.
	 * You can request a specific version by constructing and binding an instance of this class:
	 *
	 * <code><pre>binder.bind(IFigurationService.class).toInstance(new FigurationService("3.0.0-alpha.2"));
	 * </pre></code>
	 *
	 * Alternatively, subclass FigurationService and change its default.
	 */
	public FigurationService (String version) {
		super();
		figurationVersion = version;
	}

	@Override
	public JavaScriptHeaderItem makeJavaScriptReferenceHeaderItem(String version) {
		return JavaScriptHeaderItem.forReference(
				new UrlResourceReference(Url.parse(
						String.format("https://cdn.jsdelivr.net/figuration/%s/js/figuration.min.js",
								version))) {
					@Override
					public List<HeaderItem> getDependencies() {
						List<HeaderItem> dependencies = new LinkedList<>();
						dependencies.add(
								JavaScriptHeaderItem.forReference(
										Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
						return dependencies;
					}
				});
	}


	@Override
	public CssHeaderItem makeCssReferenceHeaderItem(String version) {
		return CssHeaderItem.forReference(
				new UrlResourceReference(Url.parse(
						String.format("https://cdn.jsdelivr.net/figuration/%s/css/figuration.min.css", version))));
	}

	@Override
	public void addFigurationHeaderItems(IHeaderResponse response) {
		response.render(new PriorityHeaderItem(StringHeaderItem.forString(
				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">")));
		response.render(new PriorityHeaderItem(StringHeaderItem.forString(
				"<meta http-equiv=\"x-ua-compatible\" content=\"ie=edge\">")));

		response.render(makeJavaScriptReferenceHeaderItem(getFigurationVersion()));
		response.render(makeCssReferenceHeaderItem(getFigurationVersion()));
	}

}
