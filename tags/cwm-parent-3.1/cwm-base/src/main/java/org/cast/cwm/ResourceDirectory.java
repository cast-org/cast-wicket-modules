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
package org.cast.cwm;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.PackageResource.PackageResourceBlockedException;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Represents a directory in which all of the files can be used as resources
 * (eg, images). Which file to send in response to a particular request is based
 * on the URL, which is appended to the given resource directory path.
 *
 * <p>See {@link ResourceDirectoryReference} for usage examples.</p>

 * <p>The installed PackageResourceGuard will be checked to make sure the file is
 * ok to send.</p>
 * 
 * <p>Heavily based on the
 * {@link org.apache.wicket.request.resource.PackageResource} class, but with
 * different logic to locate the file to send.</p>
 * 
 * @see ResourceDirectoryReference
 * 
 * @author bgoldowsky
 * 
 */
public class ResourceDirectory extends AbstractResource {
	private static final Logger log = LoggerFactory.getLogger(ResourceDirectory.class);

	private static final Duration DEFAULT_CACHE_DURATION = Duration.hours(1);

	private static final long serialVersionUID = 1L;

	/**
	 * The path to the directory of resources
	 */
	private final File sourceDirectory;
	
	private Duration cacheDuration = DEFAULT_CACHE_DURATION;

	/**
	 * Construct a with a given directory of resource files.
	 * 
	 * @param directory the directory containing public files
	 */
	public ResourceDirectory(File directory) {
		this.sourceDirectory = directory;
	}

	/**
	 * creates a new resource response based on the request attributes
	 * 
	 * @param attributes
	 *            current request attributes from client
	 * @return resource response for answering request
	 */
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		String relativePath = "";
		PageParameters parameters = attributes.getParameters();
		for (int i=0; i< parameters.getIndexedCount(); i++) {
			relativePath += parameters.get(i);
			relativePath += '/';
		}
		if (relativePath.endsWith("/"))
			relativePath = relativePath.substring(0, relativePath.length()-1);
		log.debug("relativePath is {}", relativePath);

		String absolutePath = new File(sourceDirectory, relativePath)
				.getAbsolutePath();

		final ResourceResponse resourceResponse = new ResourceResponse();

		final IResourceStream resourceStream = getResourceStream(absolutePath);

		// bail out if resource stream could not be found
		if (resourceStream == null) {
			return sendResourceError(absolutePath, resourceResponse,
					HttpServletResponse.SC_NOT_FOUND, "Unable to find resource");
		}

		// allow caching
		resourceResponse.setCacheScope(WebResponse.CacheScope.PUBLIC);
		resourceResponse.setCacheDuration(cacheDuration);

		// add Last-Modified header (to support HEAD requests and If-Modified-Since)
		resourceResponse.setLastModified(resourceStream.lastModifiedTime());

		if (resourceResponse.dataNeedsToBeWritten(attributes)) {
			String contentType = resourceStream.getContentType();

			if (contentType == null && Application.exists())
				contentType = Application.get().getMimeType(absolutePath);

			// set Content-Type (may be null)
			resourceResponse.setContentType(contentType);

			try {
				// read resource data
				final byte[] bytes;

				bytes = IOUtils.toByteArray(resourceStream.getInputStream());

				// send Content-Length header
				resourceResponse.setContentLength(bytes.length);

				// send response body with resource data
				resourceResponse.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(Attributes attributes) {
						attributes.getResponse().write(bytes);
					}
				});
			} catch (IOException e) {
				log.debug(e.getMessage(), e);
				return sendResourceError(absolutePath, resourceResponse, 500,
						"Unable to read resource stream");
			} catch (ResourceStreamNotFoundException e) {
				log.debug(e.getMessage(), e);
				return sendResourceError(absolutePath, resourceResponse, 500,
						"Unable to open resource stream");
			} finally {
				try {
					resourceStream.close();
				} catch (IOException e) {
					log.warn("Unable to close the resource stream", e);
				}
			}
		}

		return resourceResponse;
	}

	/**
	 * send resource specific error message and write log entry
	 * 
	 * @param resourceResponse
	 *            resource response
	 * @param errorCode
	 *            error code (=http status)
	 * @param errorMessage
	 *            error message (=http error message)
	 * @return resource response for method chaining
	 */
	private ResourceResponse sendResourceError(String absolutePath,
			ResourceResponse resourceResponse, int errorCode,
			String errorMessage) {
		String msg = String.format("resource [path = %s]: %s (status=%d)",
				absolutePath, errorMessage, errorCode);

		log.warn(msg);

		resourceResponse.setError(errorCode, errorMessage);
		return resourceResponse;
	}

	/**
	 * locate resource stream for current resource
	 * 
	 * @return resource stream or <code>null</code> if not found
	 */
	public IResourceStream getResourceStream(String absolutePath) {
		if (!accept(Application.class, absolutePath)) {
			throw new PackageResourceBlockedException(
					"Access denied to (static) package resource "
							+ absolutePath + ". See IPackageResourceGuard");
		}
		return new FileResourceStream(new File(absolutePath));
	}

	/**
	 * @param scope
	 *            resource scope
	 * @param path
	 *            resource path
	 * @return <code>true<code> if resource access is granted
	 */
	private boolean accept(Class<?> scope, String path) {
		IPackageResourceGuard guard = Application.get().getResourceSettings()
				.getPackageResourceGuard();

		return guard.accept(scope, path);
	}

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + ' ' + "dir = " + sourceDirectory + ']';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((sourceDirectory == null) ? 0 : sourceDirectory
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceDirectory other = (ResourceDirectory) obj;
		if (sourceDirectory == null) {
			if (other.sourceDirectory != null)
				return false;
		} else if (!sourceDirectory.equals(other.sourceDirectory))
			return false;

		return true;
	}

	public Duration getCacheDuration() {
		return cacheDuration;
	}

	public void setCacheDuration(Duration cacheDuration) {
		this.cacheDuration = cacheDuration;
	}

}
