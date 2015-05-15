/*
 * Copyright 2011-2015 CAST, Inc.
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
package org.cast.cwm;

import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.wicketstuff.jslibraries.JSLib;
import org.wicketstuff.jslibraries.Library;
import org.wicketstuff.jslibraries.VersionDescriptor;

/**
 * Loads a version of jQuery that is consistent across all CWM components that need it.
 * Can probably be dropped in Wicket 6 since the framework itself will depend on jQuery.
 * 
 * @author bgoldowsky
 *
 */
public class JQueryHeaderContributor implements IHeaderContributor {

	private static final long serialVersionUID = 1L;

	public void renderHead(IHeaderResponse response) {
		JSLib.getHeaderContribution(VersionDescriptor.exactVersion(Library.JQUERY, 1, 7, 1)).renderHead(response);
	}

}
