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
package org.cast.cwm.components.service;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * This class centralizes inclusions of JQuery and other Javascript libraries that we may commonly use.
 * At the moment it is very simple-minded, but may eventually be more clever about using CDNs, including 
 * multiple versions, fallbacks etc.
 * 
 * @author borisgoldowsky
 *
 */
public class JavascriptService {
	
	protected static JavascriptService instance = new JavascriptService();
	
	private boolean useCDN = true;
	
	private String defaultJQueryVersion = "1.6.4"; 
	
	private ResourceReference currentRR = null;
	
	protected JavascriptService() { } /* Protected constructor for singleton - use get() */
	
	public static JavascriptService get() {
		return instance;
	}
	
	/**
	 * Your subclass can define a method like this to use its own subclass, and call it from {@link Application#init()}.
	 */
	public static void useAsServiceInstance() {
		JavascriptService.instance = new JavascriptService();
	}

	/**
	 * Add appropriate markup to the header to include the current version of JQuery.
	 * @param response
	 */
	public void includeJQuery (IHeaderResponse response) {
		includeJQuery(response, null);
	}
		
	/**
	 * Add appropriate markup to the header to include the specified version of JQuery.
	 * @param response 
	 * @param version the version string, or null to use the default version.
	 */
	public void includeJQuery (IHeaderResponse response, String version) {
		if (useCDN) {
			if (version == null)
				version = defaultJQueryVersion;
			// Use a network-path relative reference (no "http:") so this should work efficiently on both http and https
			// recommended by encosia.com: http://tinyurl.com/4qw6xvt
			response.renderJavascriptReference("//ajax.googleapis.com/ajax/libs/jquery/" + version + "/jquery.min.js");
		} else {
			response.renderJavascriptReference(getJQueryResourceReference(version));
		}
	}

	/**
	 * Return a reference to a local copy of the current version of JQuery that we are using.
	 * @return a ResourceReference for the library
	 */
	protected ResourceReference getJQueryResourceReference () {
		if (currentRR == null)
			currentRR = getJQueryResourceReference(defaultJQueryVersion); 
		return currentRR;
	}

	/**
	 * Return a reference to a local copy of the specified version of JQuery.
	 * This method just assumes that the caller will only ask for versions that are actually locally stored in this module.
	 * @param version a version string, or null to use the default version.
	 * @return a ResourceReference for the library
	 */
	protected ResourceReference getJQueryResourceReference (String version) {
		if (version == null)
			return getJQueryResourceReference();
		else
			return new ResourceReference(JavascriptService.class, "jquery-" + version + ".min.js");
	}

	/**
	 * <p>
	 * Tell this class whether you want to load javascript libraries from a 
	 * CDN or from local files.
	 * </p>
	 * <p>
	 * <strong>Note</strong>: Changing this behavior between multiple calls
	 * of {@link #includeJQuery(IHeaderResponse)} on a page (e.g. from different 
	 * components) can lead to problems.
	 * </p>
	 * 
	 *
	 * @param useCDN true to use a CDN (true is the default)
	 */
	public void setUseCDN(boolean useCDN) {
		this.useCDN = useCDN;
	}

	public boolean isUseCDN() {
		return useCDN;
	}

	public void setDefaultJQueryVersion (String version) {
		this.defaultJQueryVersion = version;
	}
	
	public String getDefaultJQueryVersion () {
		return defaultJQueryVersion;
	}

}
