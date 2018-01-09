/*
 * Copyright 2011-2018 CAST, Inc.
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

import org.apache.wicket.extensions.markup.html.image.resource.ThumbnailImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Like {@link UploadedFileResource}, but assumes resource is an image and scales it to a maximum dimension.
 * 
 * @author bgoldowsky
 *
 */
public class ThumbnailUploadedImageResourceReference extends ResourceReference {
	
	private final int maxSize;
	
	private static final long serialVersionUID = 1L;
	
	public ThumbnailUploadedImageResourceReference(int maxSize) {
		// These arguments create a key that should be unique
		super(ThumbnailUploadedImageResourceReference.class, "@"+maxSize);
		this.maxSize = maxSize;
	}

	@Override
	public IResource getResource() {
		return new ThumbnailImageResource(new UploadedFileResource(), maxSize);
	}

}
