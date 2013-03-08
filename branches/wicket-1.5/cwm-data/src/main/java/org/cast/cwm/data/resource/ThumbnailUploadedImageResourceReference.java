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
