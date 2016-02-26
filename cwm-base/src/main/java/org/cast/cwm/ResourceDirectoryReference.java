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

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.file.File;

/**
 * Mountable {@link ResourceReference} for a {@link ResourceDirectory}.
 * 
 * <p>Example:</P
 * 
 * <code>mountResource("image", new ResourceDirectoryReference(themeDir));</code><br />
 * maps {@code http://host/context/image/foo.png} to {@code themeDir/foo.png} ; that is,
 * all files in the "img" directory are available under the "image" top-level URL path.
 * Multiple levels of subdirectories may exist under img.
 *
 * @see ResourceDirectory
 * 
 * @author bgoldowsky
 *
 */
public class ResourceDirectoryReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	
	private final File resourceDirectory;
	
	/**
	 * Construct for a given directory.
	 * @param directory the directory to map as static resource files
	 */
	public ResourceDirectoryReference (File directory) {
		super(directory.getAbsolutePath());  // Use pathname as ResourceReference key
		resourceDirectory = directory;
	}
	
	@Override
	public IResource getResource() {
		return new ResourceDirectory(resourceDirectory);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ResourceDirectoryReference))
			return false;
		ResourceDirectoryReference rdr = (ResourceDirectoryReference)other;
		return (resourceDirectory.equals(rdr.resourceDirectory));
	}
}
