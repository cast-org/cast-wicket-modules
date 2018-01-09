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

import javax.servlet.http.HttpServletResponse;

import lombok.Getter;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.service.ICwmService;

import com.google.inject.Inject;

/**
 * A Resource that serves a single BinaryFileData object.
 * 
 * TODO: reconcile this with the almost-identical UploadedFileResource
 */
public class BinaryFileDataResource extends AbstractResource {

	@Inject
	private ICwmService cwmService;

	@Getter
	protected Long id;
	
	private transient BinaryFileData bfd;
	
	private static final long serialVersionUID = 1L;
	
	public BinaryFileDataResource (Long id) {
		super();
		this.id = id;
		Injector.get().inject(this);		
	}
	
	/**
	 * @see org.apache.wicket.request.resource.AbstractResource#newResourceResponse(org.apache.wicket.request.resource.IResource.Attributes)
	 */
	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes) {
		final ResourceResponse response = new ResourceResponse();

		bfd = cwmService.getById(BinaryFileData.class, id).getObject();
		if (bfd == null)
    		throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Data not found [id=" + id + "]");
		
		String contentType = bfd.getMimeType();
		response.setContentType(contentType);

		response.setLastModified(Time.valueOf(bfd.getLastModified()));

		response.setContentLength(bfd.getData().length);

		if (response.dataNeedsToBeWritten(attributes)) {
			// Should we set file name like ByteArrayResource does?
						
			response.setWriteCallback(new WriteCallback() {
				@Override
				public void writeData(final Attributes attributes) {
					attributes.getResponse().write(bfd.getData());
				}
			});
		}

		return response;
	}

}
