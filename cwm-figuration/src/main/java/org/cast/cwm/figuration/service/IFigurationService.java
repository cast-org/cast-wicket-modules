/*
 * Copyright 2011-2020 CAST, Inc.
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

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

/**
 * Injectable service that provides access to Figuration header items.
 *
 * @author bgoldowsky
 */
public interface IFigurationService {

	/**
	 * Return the version identifier of the figuration version to be used.
	 * @return version number as a string.
	 */
	String getFigurationVersion();

	/**
	 * Construct a JavaScriptHeaderItem for the figuration library's minified Javascript.
	 * @param version Figuration version identifier
	 * @return header item
	 */
	JavaScriptHeaderItem makeJavaScriptReferenceHeaderItem(String version);

	/**
	 * Construct a CssHeaderItem for the figuration library's CSS.
	 * @param version Figuration version identifier
	 * @return header item
	 */
	CssHeaderItem makeCssReferenceHeaderItem(String version);

	/**
	 * Add the header items needed to use the CAST Figuration framework.
	 * Uses the default version of figuration as defined in this class.
	 * @param response the IHeaderResponse that will have items added to it
	 */
	void addFigurationHeaderItems(IHeaderResponse response);

}
