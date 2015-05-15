/*
 * Copyright 2011-2015 CAST, Inc.
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

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.service.ICwmService;

import com.google.inject.Inject;

/**
 * A simple resource that accepts an "id" parameter and serves up
 * the file with the matching ID in the database.  If the file is
 * not found, this will throw a 404 Not Found Error.
 * 
 * TODO: reconcile this with the almost-identical BinaryFileDataResource
 * 
 * @author jbrookover
 *
 */
public class UploadedFileResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	@Inject
	private ICwmService cwmService;
	
	public UploadedFileResource() {
		super();
		Injector.get().inject(this);

		// TODO is this still true in Wicket 1.5?
		// Cannot be cacheable, otherwise WicketFilter will cause database access when it checks the last-modified
		// date, before the session context has been set up, and this database session can remain unclosed.
		// setCacheable(true);
	}

	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes) {
		final ResourceResponse response = new ResourceResponse();

		long id = attributes.getParameters().get("id").toLong();
		IModel<BinaryFileData> mBfd = cwmService.getById(BinaryFileData.class, id);
		if (mBfd == null || mBfd.getObject() == null) {
			response.setError(HttpServletResponse.SC_NOT_FOUND);
			return response;
		}
		BinaryFileData bfd = mBfd.getObject();
		response.setLastModified(Time.valueOf(bfd.getLastModified()));
		
		if (response.dataNeedsToBeWritten(attributes)) {
			response.setContentType(bfd.getMimeType());
			response.setContentDisposition(ContentDisposition.INLINE);
			final byte[] imageData = bfd.getData();			
			if (imageData == null) {
				response.setError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				response.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(final Attributes attributes) {
						attributes.getResponse().write(imageData);
					}
				});
			}
		}
		return response;
	}

	// FIXME this can't be right
	@Override
	public boolean equals(Object that) {
		return that instanceof SvgImageResource;
	}

}
