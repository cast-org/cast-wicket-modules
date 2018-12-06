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
package org.cast.cwm.data.resource;

import com.google.inject.Inject;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.PartWriterCallback;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.service.ICwmService;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;

/**
 * A simple resource that accepts an "id" parameter and serves up
 * the file with the matching ID in the database.  If the file is
 * not found, this will throw a 404 Not Found Error.
 *
 * Note: in Wicket 1.4 this could not be cacheable, since WicketFilter would cause database access
 * outside of a proper session context when it checked the last-modified time; this database session
 * would remain unclosed.  I believe this is no longer a problem in Wicket 6.
 * 
 * @author jbrookover
 * @author bgoldowsky
 *
 */
public class UploadedFileResource extends AbstractResource {

	@Inject
	private ICwmService cwmService;
	
	public UploadedFileResource() {
		super();
		Injector.get().inject(this);
	}

	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes) {
		ResourceResponse response = new ResourceResponse();

		long id;
		try {
			id = attributes.getParameters().get("id").toLong();
		} catch (StringValueConversionException e) {
			throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND,
					"Invalid id in request [id=" + attributes.getParameters().get("id")+ "]");
		}

		IModel<BinaryFileData> mBfd = cwmService.getById(BinaryFileData.class, id);
		if (mBfd == null || mBfd.getObject() == null)
			throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND,
					"Data not found [id=" + id + "]");
		BinaryFileData bfd = mBfd.getObject();

        response.setAcceptRange(ContentRangeType.BYTES);
		response.setLastModified(Time.valueOf(bfd.getLastModified()));

		if (response.dataNeedsToBeWritten(attributes)) {

			final byte[] data = bfd.getData();
			if (data == null)
				throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND,
						"Data not found [id=" + id + "]");

			response.setContentType(bfd.getMimeType());
			response.setContentLength(data.length);
			response.setContentDisposition(ContentDisposition.INLINE);
			response.setCacheDuration(Duration.days(1));
			response.setCacheScope(WebResponse.CacheScope.PUBLIC);
            RequestCycle cycle = RequestCycle.get();
            Long startbyte = cycle.getMetaData(CONTENT_RANGE_STARTBYTE);
            Long endbyte = cycle.getMetaData(CONTENT_RANGE_ENDBYTE);
            response.setWriteCallback(
                    new PartWriterCallback(new ByteArrayInputStream(data), (long)data.length, startbyte, endbyte));
		}
		return response;
	}

}
