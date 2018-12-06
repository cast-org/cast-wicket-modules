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
package org.cast.cwm.xml;

import lombok.ToString;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.cast.cwm.IRelativeLinkSource;

import java.io.InputStream;

@ToString
public class FileResource extends PackageResource implements IRelativeLinkSource {

	private static final long serialVersionUID = 1L;
	private File file;
	
	public FileResource (File f) {
		super(FileResource.class, f.getAbsolutePath(), null, null, null);
		file = f;
	}
	
	@Override
	public IResourceStream getResourceStream () { 
		return new FileResourceStream(file);
	}

	/**
	 * Return a FileResource at a location relative to this one.
	 */
	public FileResource getRelativeResource (String relativePath) {
		return new FileResource(getRelativeFile(relativePath));
	}

	/**
	 * Return an InputStream for a file relative to this one.
	 */
	@Override
	public InputStream getInputStream(String relativePath) {
		return null;
	}

	/** Return a ResourceReference to a relatively-addressed item
	 * 
	 * @param relativePath path relative to the path of this Resource
	 * @return a ResourceReference that will resolve to {@link #getRelative(relativePath)}
	 */
	@Override
	public ResourceReference getRelativeReference (final String relativePath) {
		String filePath = getRelativeFile(relativePath).getAbsolutePath().substring(1);
		return new ResourceReference(FileResource.class, filePath) {
			private static final long serialVersionUID = 1L;
			@Override
			public IResource getResource() {
				return getRelativeResource(relativePath);
			}
		};
	}

	private File getRelativeFile (String relativePath) {
		return new File(file.getParentFile(), relativePath);
	}
	
}
