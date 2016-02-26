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
package org.cast.cwm.data.resource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Mountable reference to the {@link UploadedFileResource} class.
 * UploadedFileResource expects an id parameter, so mount it like this:
 * <pre>
 *     mountResource("/uploads/${id}", new UploadedFileResourceReference());
 * </pre>
 * 
 * @author bgoldowsky
 *
 */
public class UploadedFileResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;
	
	public UploadedFileResourceReference() {
		// These arguments create a key that should be unique
		super(UploadedFileResourceReference.class, "file");
	}

	@Override
	public IResource getResource() {
		return new UploadedFileResource();
	}

}
