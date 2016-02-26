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
package org.cast.cwm;

import java.io.InputStream;

import org.apache.wicket.request.resource.ResourceReference;

/**
 * An object (typically a Resource) that can be used to locate resources via relative links.
 * This is used eg for XML files that contain links to images.  These relative links can be
 * used to obtain a ResourceReference or InputStream for those images.
 *
 * @author bgoldowsky
 *
 */
public interface IRelativeLinkSource {

	public ResourceReference getRelativeReference (String relativePath);
	
	public InputStream getInputStream (String relativePath) 
			throws InputStreamNotFoundException;

}
