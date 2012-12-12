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
package org.cast.cwm.data.resource;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.service.CwmService;
import org.cast.cwm.service.ICwmService;

import com.google.inject.Inject;

/**
 * A simple resource that accepts an "id" parameter and serves up
 * the file with the matching ID in the database.  If the file is
 * not found, this will throw a 404 Not Found Error.
 * 
 * TODO: Compare for quality with https://cwiki.apache.org/WICKET/uploaddownload.html as it was suggested by Igor
 * 
 * @author jbrookover
 *
 */
public class UploadedFileResource extends DynamicWebResource {

	private static final long serialVersionUID = 1L;
	public static final String UPLOAD_FILE_PATH = "file";
	private static boolean mounted = false;

	@Inject
	private ICwmService cwmService;

	/**
	 * Constructor.  Turns caching on by default,
	 * overriding superclass behavior.  
	 */
	public UploadedFileResource() {
		super();
		InjectorHolder.getInjector().inject(this);
		// Cannot be cacheable, otherwise WicketFilter will cause database access when it checks the last-modified
		// date, before the session context has been set up, and this database session can remain unclosed.
		// setCacheable(true);
	}
	
	@Override
	protected ResourceState getResourceState() {
		
		// Check ID parameter; throw 404 if invalid
		Long id = getParameters().getAsLong("id");		
		if (id == null)
			throw new AbortWithWebErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Invalid File Id");	
		
		// Get Data; throw 404 if not found
		final BinaryFileData bfd = cwmService.getById(BinaryFileData.class, id).getObject();
		if (bfd == null)
			throw new AbortWithWebErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "File not found [id=" + id + "]");
		
		return new ResourceState() {

			@Override
			public String getContentType() { return bfd.getMimeType();}

			@Override
			public byte[] getData() { return bfd.getData();}
			
			@Override
			public Time lastModifiedTime() { return Time.valueOf(bfd.getLastModified());}
		};
	}

	public static void mount(WebApplication app) {
		app.getSharedResources().add(UPLOAD_FILE_PATH, new UploadedFileResource());
		app.mountSharedResource("/" + UPLOAD_FILE_PATH, Application.class.getName() + "/" + UPLOAD_FILE_PATH);
		mounted = true;
	}
		
	public static String constructUrl(BinaryFileData fileData) {
		if (!mounted)
			throw new IllegalStateException("UploadedFileResource has not been mounted in the Application.");
		return UPLOAD_FILE_PATH + "/id/" + fileData.getId();
	}
}
