/*
 * Copyright 2011-2013 CAST, Inc.
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
 * ResourceReference for a ResourceDirectory.
 * For example, to make all the files in an "images" directory available under the URL path "img", 
 * you can mount this as follows:
 * <blockquote><code>
 *    mountResource("img/${name}", new ResourceDirectoryReference(themeDirectory, "images"));
 * </code></blockquote>
 * @author borisgoldowsky
 *
 */
public class ResourceDirectoryReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	
	private final File resourceDirectory;

	/**
	 * Construct for a given directory.
	 * @param directory
	 */
	public ResourceDirectoryReference(File directory) {
		super(directory.getAbsolutePath());  // Use pathname as ResourceReference key
		resourceDirectory = directory;
	}

	/**
	 * Convenience constructor where a base directory and subdirectory within it are specified.
	 * Useful if you are building several ResourceDirectoryReferences that are siblings to each other.
	 * @param themeDir base directory within which the resources directory is found
	 * @param subdirectory name of the subdirectory
	 */
	public ResourceDirectoryReference (File themeDir, String subdirectory) {
		this(new File(themeDir, subdirectory));
	}
	
	@Override
	public IResource getResource() {
		return new ResourceDirectory(resourceDirectory);

	}

}
