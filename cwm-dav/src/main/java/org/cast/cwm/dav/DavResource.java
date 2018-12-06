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
package org.cast.cwm.dav;

import com.xmlmind.davclient.*;
import org.apache.wicket.Application;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.InputStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A Resource for a file on a DAV server.
 * 
 * @author bgoldowsky
 *
 */
public class DavResource extends AbstractResource implements IInputStreamProvider, IRelativeLinkSource {

	protected final String clientName;
	protected final String path;
	protected String contentType = null;
	protected Time lastModified = null;       // our view of when file last changed
	protected Time serverLastModified = null; // what server is reporting about last modification time
	protected Long fileSize = null;
	
	protected Long serverCheckInterval = 30L;  // how many seconds between checks of the DAV server for modifications
	protected Long lastServerCheck = null;
	
	// Map of related resources that have been found before - so we return the same one for the same query each time.
	protected Map<String, DavResource> resourceMap = new HashMap<String, DavResource>();
	
	protected static final QName[] interestingProperties = { DAVClient.GETLASTMODIFIED_PROP, DAVClient.GETCONTENTLENGTH_PROP };

	private static final Logger log = LoggerFactory.getLogger(DavResource.class);
	private static final long serialVersionUID = 1L;

	public DavResource (String clientName, String path) {
		super();
		this.clientName = clientName;
		this.path = path;
		if (!path.startsWith("/"))
			throw new IllegalArgumentException("DAV Resource path name must be absolute; provided path was " + path);
	}

	/**
	 * @see org.apache.wicket.request.resource.AbstractResource#newResourceResponse(org.apache.wicket.request.resource.IResource.Attributes)
	 */
	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes) {
		final ResourceResponse response = new ResourceResponse();
		
		// Set last modified time so that response can determine if data needs to be written.
		response.setLastModified(lastModifiedTime());
		
		if (response.dataNeedsToBeWritten(attributes)) {
			DavResourceStream resourceStream = new DavResourceStream();
			final byte[] bytes;
			try {
				bytes = IOUtils.toByteArray(resourceStream.getInputStream());
				response.setContentLength(bytes.length);
				response.setLastModified(lastModified);			
				if (Application.exists())
					response.setContentType(Application.get().getMimeType(path));
				
				response.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(Attributes attributes) {
						attributes.getResponse().write(bytes);
					}
				});
			} catch (IOException e) {
				log.error("I/O Exception while trying to read DAV resource");
				e.printStackTrace();
				response.setError(404);
			} catch (ResourceStreamNotFoundException e) {
				log.error("DAV resource not found: " + path);
				response.setError(404);
			} finally {
				try {
					resourceStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}		
		return response;
	}

	public DAVClient getClient() {
		return DavClientManager.get().getClient(clientName);
	}

	@Override
	public Time lastModifiedTime() {
		retrieveProperties();
		return lastModified;
	}
	
	public Long getFileSize() {
		retrieveProperties();
		return fileSize;
	}
	
	/**
	 * Return an InputStream for this resource.
	 */
	@Override
	public InputStream getInputStream() throws InputStreamNotFoundException {
		try {
			return new DavResourceStream().getInputStream();
		} catch (ResourceStreamNotFoundException e) {
			throw new InputStreamNotFoundException(e);
		}
	}

	/**
	 * Return an InputStream for a resource relative to this one.
	 */
	@Override
	public InputStream getInputStream(String relativePath) throws InputStreamNotFoundException {
		return getRelativeResource(relativePath).getInputStream();
	}
	
	/** Return a ResourceReference to a relatively-addressed item
	 * 
	 * @param relativePath path relative to the path of this Resource
	 * @return a ResourceReference that will resolve to {@link #getRelative(relativePath)}
	 */
	@Override
	public ResourceReference getRelativeReference (final String relativePath) {
		String childPath = path.substring(1, path.lastIndexOf('/')+1) + relativePath;
		return new ResourceReference (DavResource.class, childPath) {
			private static final long serialVersionUID = 1L;
			@Override
			public IResource getResource() {
				return getRelativeResource(relativePath);
			}
		};
	}
	
	/**
	 * Return a Resource accessed via a relative path from this Resource.
	 * Each Resource created for such queries will be remembered, and
	 * the same Resource will be returned again for a later query.  This allows for
	 * the child Resources to do caching.
	 */
	public DavResource getRelativeResource(String relativePath) {
		if (resourceMap.containsKey(relativePath)) {
			return resourceMap.get(relativePath);
		} else {
			DavResource relative = new DavResource (clientName, path.substring(0, path.lastIndexOf('/')+1) + relativePath);
			resourceMap.put(relativePath, relative);
			return relative;
		}
	}
	
	protected void setMetadata (Time newLastModified, long newFileSize) {
		if (serverLastModified==null || newLastModified.after(serverLastModified)) {
			// Server's update time has changed
			fileSize = newFileSize;
			serverLastModified = newLastModified;
			lastModified = newLastModified;
		} else if (newFileSize != fileSize) {
			// Odd situation -- last modified time reported the same, but size has changed.
			// XDR actually gets into this state if updates have been stored without creating a new "version"
			// Update our information on modification time so that the webapp can see the changes immediately.
			log.debug("File size changed: setting current time as last modification");
			fileSize = newFileSize;
			lastModified = Time.now();
			// serverLastModified left as is, so that we can continue to recognize further stealthy updates.
		}
	}
	
	// Look up properties of this content object's file on the server if we haven't recently done so.
	synchronized protected void retrieveProperties() {
		long time = new Date().getTime();
		if (lastServerCheck == null
				|| (time > lastServerCheck + serverCheckInterval*1000L)) {
			Property[] props = getProperties(path);
			if (props != null) {
				setMetadata(getLastModifiedValue(props), getLengthValue(props));
			} else {
				log.warn("Server returned no properties for {}", path);
			}
			lastServerCheck = time;
		}
	}

	/**
	 * Get interesting properties of a given filePath.  Returns null if the file doesn't exist.
	 * @param filePath
	 * @return
	 */
	protected Property[] getProperties(String filePath) {
		PropertyList[] props = null;
		try {
			props = getClient().propfind(filePath, interestingProperties, 0);
			log.debug("PROPFIND on {}", filePath);
		} catch (DAVException e) {
			// There is one error type that we can actually handle: a 404 indicating the object was not found.
			if (e.errors[0].statusCode == 404)
				return null;
			throw new RuntimeException("Can't get properties", e);
		} catch (IOException e) {
			throw new RuntimeException("Can't get properties", e);
		}
		if (props.length < 1)
			throw new RuntimeException("No properties returned");

		// We're only interested in props[0] since we've only requested info about a single resource.
		return props[0].properties;
	}

	protected Time getLastModifiedValue(Property[] properties) {
		return Time.valueOf ((Date) getValue(Constants.GETLASTMODIFIED_PROP, properties));
	}

	protected Long getLengthValue (Property[] properties) {
		return (Long) getValue(Constants.GETCONTENTLENGTH_PROP, properties);
	}

	// Method adapted from src/com/xmlmind/davclient/DavClientTool.java
	// for each property, name has getNamespaceURI() and getLocalPart(); then there's a value which can be cast 
	// (Long)getValue(GETCONTENTLENGTH_PROP, properties)
	// (Date)getValue(GETLASTMODIFIED_PROP, properties)
	protected Object getValue(QName name, Property[] properties) {
		String namespace = name.getNamespaceURI();
		String localName = name.getLocalPart();
		for (Property property : properties) {
			if (property.name.getNamespaceURI().equals(namespace) &&
					property.name.getLocalPart().equals(localName))
				return property.value;
		}
		return null;
	}

	protected class DavResourceStream extends AbstractResourceStream {

		protected InputStream stream = null;

		private static final long serialVersionUID = 1L;

		@Override
		public void close() throws IOException {
			if (stream != null)
				stream.close();
		}

		@Override
		public InputStream getInputStream() throws ResourceStreamNotFoundException {
			Content content = null;
			try {
				content = getClient().get(path);
				log.debug("GET file content for {}", path);
			} catch (IOException e) {
				throw new ResourceStreamNotFoundException(e);
			} catch (DAVException e) {
				throw new ResourceStreamNotFoundException(e);
			}
			// Update file size and mod date based on the Content object
			setMetadata (Time.millis(content.getContentDate()), content.getContentLength());
			contentType = content.getContentType();
			try {
				stream = content.openContent();
				return stream;
			} catch (IOException e) {
				e.printStackTrace();
				throw new ResourceStreamNotFoundException(e);
			}
		}

		@Override
		public Bytes length() {
			return Bytes.bytes(getFileSize());
		}

	}

}
