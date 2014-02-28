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
package org.cast.cwm;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.file.File;

/**
 * Mountable {@link ResourceReference} for a {@link ResourceDirectory}.
 * 
 * <p>Examples:</P
 * 
 * <ul>
 * <li><code>mountResource("img", new ResourceDirectoryReference(themeDir));</code><br />
 * maps {@code http://host/context/img/foo.png} to {@code themeDir/img/foo.png} ; that is,
 * all files in the "img" directory are available under the "img" top-level URL path.</li>
 * 
 * <li><code>mountResource("static", new ResourceDirectoryReference(themeDir, "static"));</code><br />
 * maps {@code http://host/context/static/foo.png} to {@code themeDir/foo.png} .
 * This form is useful if your directory name doesn't match the URL you want to mount it on.</li>
 * </ul>
 *
 * @see ResourceDirectory
 * 
 * @author bgoldowsky
 *
 */
public class ResourceDirectoryReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	
	private final File resourceDirectory;
	
	private final String removePrefix;

	/**
	 * Construct for a given directory.
	 * @param themeDirectory
	 */
	public ResourceDirectoryReference(File themeDirectory) {
		this(themeDirectory, "");
	}

	/**
	 * Convenience constructor where a base directory and subdirectory within it are specified.
	 * Useful if you are building several ResourceDirectoryReferences that are siblings to each other.
	 * @param themeDir base directory within which the resources directory is found
	 * @param subdirectory name of the subdirectory
	 */
	public ResourceDirectoryReference (File directory, String removePrefix) {
		super(directory.getAbsolutePath());  // Use pathname as ResourceReference key
		resourceDirectory = directory;
		this.removePrefix = removePrefix;
	}
	
	@Override
	public IResource getResource() {
		return new ResourceDirectory(resourceDirectory, removePrefix);

	}

}
