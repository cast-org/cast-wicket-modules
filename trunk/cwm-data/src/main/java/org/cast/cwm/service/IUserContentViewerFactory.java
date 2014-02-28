/*
 * Copyright 2011-2014 CAST, Inc.
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
import org.cast.cwm.data.UserContent;

public interface IUserContentViewerFactory {
	
	/**
	 * Check whether this factory can return a viewing component for the given @UserContent object.
	 * @param mUserContent
	 * @return true if {@link #makeContentViewer} can return a viewer Component for this UserContent
	 */
	public <T extends UserContent> boolean canHandle (IModel<T> mUserContent);
	
	/**
	 * Return an appropriate component for viewing the given @UserContent object.
	 * Maximum width and height parameters are just advisory, and probably only apply to
	 * image-type components.  They can be null, in which case a default size should be used.
	 * 
	 * The Component returned should be something like a @Panel, so that it can replace a <div> in the markup.
	 * 
	 * @param wicketId
	 * @param mUserContent
	 * @param maxWidth
	 * @param maxHeight
	 * @return a Component that will display the UserContent
	 */
	public <T extends UserContent> Component makeContentViewer (String wicketId, IModel<T> mUserContent, Integer maxWidth, Integer maxHeight);

}
