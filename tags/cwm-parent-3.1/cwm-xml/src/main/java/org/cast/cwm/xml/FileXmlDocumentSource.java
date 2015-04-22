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
package org.cast.cwm.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import lombok.Getter;
import lombok.ToString;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.InputStreamNotFoundException;

@ToString
public class FileXmlDocumentSource implements IInputStreamProvider, IRelativeLinkSource {

	private static final long serialVersionUID = 1L;
	
	@Getter
	private File file;
	
	public FileXmlDocumentSource (File f) {
		file = f;
	}
	
	@Override
	public InputStream getInputStream () throws InputStreamNotFoundException { 
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new InputStreamNotFoundException(e); 
		}
	}

	@Override
	public InputStream getInputStream(String relativePath)
			throws InputStreamNotFoundException {
		try {
			return getRelativeResource(relativePath).getResourceStream().getInputStream();
		} catch (ResourceStreamNotFoundException e) {
			throw new InputStreamNotFoundException(e);
		}
	}

	@Override
	public Time lastModifiedTime() {
		return file.lastModifiedTime();
	}
	
	/**
	 * Return a FileResource at a location relative to this one.
	 */
	public FileResource getRelativeResource (String relativePath) {
		File fullPath = new File(file.getParentFile(), relativePath);
		return new FileResource(fullPath);
	}
	
	/** Return a ResourceReference to a relatively-addressed item
	 * 
	 * @param relativePath path relative to the path of this Resource
	 * @return a ResourceReference that will resolve to {@link #getRelative(relativePath)}
	 */
	@Override
	public ResourceReference getRelativeReference (final String relativePath) {
		String filePath = new File(file.getParentFile(), relativePath).getAbsolutePath().substring(1);
		return new ResourceReference(FileXmlDocumentSource.class, filePath) {
			private static final long serialVersionUID = 1L;
			@Override
			public IResource getResource() {
				return getRelativeResource(relativePath);
			}
		};
	}

}
