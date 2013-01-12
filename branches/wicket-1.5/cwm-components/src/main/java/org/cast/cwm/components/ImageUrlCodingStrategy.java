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
package org.cast.cwm.components;

import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.basic.URIRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.PackageResourceStream;

/**
 * Strategy to find resources based on a simple URL path.
 * This allows for example a directory of images to be found by URLs in
 * your markup file that begin with a known prefix.
 * 
 * For instance, if you have images under an "img" directory in a "theme directory",
 * and have code like this in <code>Application.init()</code>:
 * <pre>
 *     getResourceSettings().addResourceFolder("/mythemedir");
 *     mount(new ImageUrlCodingStrategy("img"));
 * </pre>
 * then an image reference in your markup file like this
 * <pre>
 *     &lt;img src="img/logos/logo.png"/>
 * </pre>
 * will refer to an image in that them directory (<code>/mythemedir/img/etc</code>), 
 * rather than Wicket's default of looking for the image relative to the 
 * current HTML markup file.
 * 
 * @author jbrookover
 *
 */
public class ImageUrlCodingStrategy extends URIRequestTargetUrlCodingStrategy {

	public ImageUrlCodingStrategy(String mountPath) {
		super(mountPath);
	}
	
	@Override
	public IRequestTarget decode(RequestParameters requestParameters) {
		String path = getURI(requestParameters);
		return new ResourceStreamRequestTarget(new PackageResourceStream(Application.class, "/" + getMountPath() + "/" + path)) {

			@Override
			protected void configure(RequestCycle requestCycle, Response response, IResourceStream resourceStream) {
				super.configure(requestCycle, response, resourceStream);
				if (response instanceof WebResponse)
					setHeaders((WebResponse)response);
			}			
		};
	}
	
	/**
	 * Set any additional headers for image responses.
	 * These are based on the caching settings in {@link org.apache.wicket.markup.html.WebResource}
	 * @param response
	 */
	protected void setHeaders(WebResponse response) {
		response.setDateHeader("Expires", System.currentTimeMillis() + (getCacheDuration() * 1000L));
		if (Application.get().getResourceSettings().getAddLastModifiedTimeToResourceReferenceUrl()) {
				response.setHeader("Cache-Control", "public,max-age=" + getCacheDuration());
		} else {
			response.setHeader("Cache-Control", "max-age=" + getCacheDuration());
		}

	}

	/**
	 * Return cache duration in seconds.
	 * This will be sent as an Expires: header, and indicates how long browsers 
	 * should wait before re-checking with the server
	 * to see if the image has been changed. 
	 * @return number of seconds to cache image.
	 */
	protected int getCacheDuration()
	{
		return Application.get().getResourceSettings().getDefaultCacheDuration();
	}
	
}
