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
package org.cast.cwm.figuration.service;

import lombok.Getter;
import org.apache.wicket.Application;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of IFigurationService.
 *
 * @author bgoldowsky
 */
public class FigurationService implements IFigurationService {

	private static final String FIGURATION_DEFAULT_VERSION = "4.0.0-alpha.6";
	private static final String POPPER_DEFAULT_VERSION = "1.15.0";

	@Getter
	private final String figurationVersion;

	@Getter
    private String popperVersion;

	/**
	 * Default constructor.
	 * Will set the library to use the default Figuration version.
	 */
	public FigurationService () {
		this(FIGURATION_DEFAULT_VERSION, POPPER_DEFAULT_VERSION);
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
	public FigurationService (String figurationVersion, String popperVersion) {
		super();
		this.figurationVersion = figurationVersion;
		this.popperVersion = popperVersion;
	}

	@Override
	public JavaScriptHeaderItem makeJavaScriptReferenceHeaderItem(String version) {
	    // Figuration has two dependencies.
        JavaScriptReferenceHeaderItem jqueryLibrary = JavaScriptHeaderItem.forReference(
                Application.get().getJavaScriptLibrarySettings().getJQueryReference());

        JavaScriptReferenceHeaderItem popperLibrary = JavaScriptHeaderItem.forReference(
                new UrlResourceReference(Url.parse(
                        String.format("https://cdn.jsdelivr.net/npm/popper.js@%s/dist/umd/popper.min.js",
                                popperVersion))));

        return JavaScriptHeaderItem.forReference(
				new UrlResourceReference(Url.parse(
						String.format("https://cdn.jsdelivr.net/npm/figuration@%s/dist/js/figuration.min.js",
								version))) {
					@Override
					public List<HeaderItem> getDependencies() {
                        return Arrays.asList(
                                jqueryLibrary,
                                popperLibrary);
					}
				});
	}


	@Override
	public CssHeaderItem makeCssReferenceHeaderItem(String version) {
		return CssHeaderItem.forReference(
				new UrlResourceReference(Url.parse(
						String.format("https://cdn.jsdelivr.net/npm/figuration@%s/dist/css/figuration.min.css", version))));
	}

	@Override
	public void addFigurationHeaderItems(IHeaderResponse response) {
		response.render(new PriorityHeaderItem(StringHeaderItem.forString(
				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">")));

		response.render(new PriorityHeaderItem(makeJavaScriptReferenceHeaderItem(getFigurationVersion())));
		response.render(new PriorityHeaderItem(makeCssReferenceHeaderItem(getFigurationVersion())));
	}

}
