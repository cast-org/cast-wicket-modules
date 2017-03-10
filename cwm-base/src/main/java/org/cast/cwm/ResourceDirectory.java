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
package org.cast.cwm;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.FileSystemResource;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * <p>Represents a directory in which all of the files can be used as resources
 * (eg, images). Which file to send in response to a particular request is based
 * on the URL, which is appended to the given resource directory path.
 *
 * <p>See {@link ResourceDirectoryReference} for usage examples.</p>

 * <p>Extends FileSystemResource so that we inherit its handling for range requests.
 * Adds caching, sending of last-modified time, and proper 404 errors.</p>
 *
 * @see ResourceDirectoryReference
 * 
 * @author bgoldowsky
 * 
 */
public class ResourceDirectory extends FileSystemResource {
	private static final Logger log = LoggerFactory.getLogger(ResourceDirectory.class);

	private static final Duration DEFAULT_CACHE_DURATION = Duration.hours(1);

	/**
	 * The path to the directory of resources
	 */
	private final Path sourceDirectory;
	
	private Duration cacheDuration = DEFAULT_CACHE_DURATION;

	/**
	 * Construct a with a given File pointing to a directory of resource files.
	 * 
	 * @param directory the directory containing public files
	 */
	public ResourceDirectory(File directory) {
		this(Paths.get(directory.getAbsolutePath()));
	}

	/**
	 * Construct a with the given Path to a directory of resource files.
	 *
	 * @param directory the directory containing public files
	 */
	public ResourceDirectory(Path directory) {
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
		Path filePath = sourceDirectory;
		PageParameters parameters = attributes.getParameters();
		for (int i=0; i< parameters.getIndexedCount(); i++) {
			filePath = filePath.resolve(parameters.get(i).toString());
		}
		log.trace("Resource file path is {}", filePath);
		if (!Files.isReadable(filePath)) {
			log.warn("Resource request for non-existent file: {}", filePath);
			throw new AbortWithHttpErrorCodeException(404, "File does not exist or is not readable");
		}

		ResourceResponse resourceResponse = null;
		try {
			resourceResponse = createResourceResponse(filePath);

			// allow caching
			resourceResponse.setCacheScope(WebResponse.CacheScope.PUBLIC);
			resourceResponse.setCacheDuration(cacheDuration);

			// add Last-Modified header (to support HEAD requests and If-Modified-Since)
			resourceResponse.setLastModified(
					Time.millis(Files.readAttributes(filePath, BasicFileAttributes.class)
							.lastModifiedTime().toMillis()));
		} catch (IOException e) {
			sendResourceError(filePath, resourceResponse, 404, e.getMessage());
		}
		return resourceResponse;
	}

	/**
	 * Send a resource-specific error message and write log entry.
	 *
	 * @param filePath path to the file that caused the error
	 * @param resourceResponse resource response on which to report the error
	 * @param errorCode http status that will be reported
	 * @param errorMessage human-readable error message
	 * @return resource response for method chaining
	 */
	private ResourceResponse sendResourceError(Path filePath,
											   ResourceResponse resourceResponse, int errorCode,
											   String errorMessage) {
		String msg = String.format("resource [path = %s]: %s (status=%d)",
				filePath, errorMessage, errorCode);

		log.warn(msg);

		if (resourceResponse != null)
			resourceResponse.setError(errorCode, errorMessage);
		return resourceResponse;
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
