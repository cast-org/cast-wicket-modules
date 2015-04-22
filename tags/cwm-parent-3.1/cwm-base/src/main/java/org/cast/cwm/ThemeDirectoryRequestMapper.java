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

import java.io.File;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.apache.wicket.request.resource.PackageResource.PackageResourceBlockedException;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A request mapper for static files in a filesystem directory.
 * It will response to any request that starts with a given set of prefixes, for example you 
 * might set up CSS and JS files to be served from a fixed location as follows:
 * <code><pre>
 *   getRootRequestMapperAsCompound().add(new ThemeDirectoryRequestMapper(themeDir, "js", "css"));
 * </pre></code>
 * 
 * Cache duration for files can be set, will default to {@link IResourceSettings#getDefaultCacheDuration()}.
 * Files are checked against the PackageResourceGuard before being delivered.
 *  
 * @author bgoldowsky
 *
 * @deprecated The ResourceDirectoryReference accomplishes the same thing in a simpler way,
 * and does a better job with setting cache headers.
 *
 */
public class ThemeDirectoryRequestMapper extends AbstractMapper {

	protected final String themeDirectory;
	
	protected final String[] prefixes;
	
	protected Duration cacheDuration = null;
	
	protected static final int COMPATIBILITY_SCORE = 7;

	private static final Logger log = LoggerFactory.getLogger(ThemeDirectoryRequestMapper.class);
	
	// flag to ignore warning when using a custom directory mapper with this standard mapper
	protected boolean logWarning = true;

	public ThemeDirectoryRequestMapper(File themeDirectory, String... prefixes) {
		super();
		this.themeDirectory = themeDirectory.getAbsolutePath();
		if (prefixes == null)
			throw new IllegalArgumentException("List of prefixes cannot be empty");
		this.prefixes = prefixes;		
	}

	// Mapper is compatible with any URL that starts with one of the prefixes
	@Override
	public int getCompatibilityScore(Request request) {
		String url = request.getUrl().toString();
		for (String prefix : prefixes)
			if (url.startsWith(prefix))
				return COMPATIBILITY_SCORE;
		return 0;
	}

	@Override
	public IRequestHandler mapRequest(Request request) {
		String path = request.getUrl().getPath();
		for (String prefix : prefixes) {
			if (path.startsWith(prefix)) {
				IResourceStream stream = getResourceStream(themeDirectory + "/" + path);
				if (stream==null)
					return null;
				ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(stream);
				if (cacheDuration != null)
					handler.setCacheDuration(cacheDuration);
				return handler;
			}
		}
		return null;
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler) {
		// TODO can we reverse-map requests like this?   Perhaps if we generate something more sophisticated than the ResourceStreamRequestHandler?
		return null;
	}
	
	public IResourceStream getResourceStream (String absolutePath) {
		if (accessCheck(absolutePath) == false) {
			throw new PackageResourceBlockedException(
					"Access denied to (static) package resource "
							+ absolutePath + ". See IPackageResourceGuard");
		}
		File file = new File(absolutePath);
		if (file.canRead()) {
			// The standard contentType determination shockingly fails to recognize CSS and JS
			// TODO: should we report this fix upstream to Wicket?
			FileResourceStream rs = new FileResourceStream(file) {
				private static final long serialVersionUID = 1L;
				@Override
				public String getContentType() {
					String fileName = getFile().getName();
					if (fileName.endsWith(".css"))
						return "text/css";
					if (fileName.endsWith(".js"))
						return "text/javascript";
					return super.getContentType();
				}
			};
			return rs;
		}
		if (logWarning)
			log.warn("Nonexistent theme file requested: {}", file);
		return null;
	}

	/**
	 * Determine if this file is OK to send.
	 * @param path resource path
	 * @return <code>true<code> if resource access is granted
	 */
	private boolean accessCheck (String path) {
		IPackageResourceGuard guard = Application.get().getResourceSettings()
				.getPackageResourceGuard();
		return guard.accept(Application.class, path);
	}

	public Duration getCacheDuration () {
		return cacheDuration;
	}

	public ThemeDirectoryRequestMapper setCacheDuration (Duration cacheDuration) {
		this.cacheDuration = cacheDuration;
		return this;
	}


}
