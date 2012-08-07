/*
 * Copyright 2011 CAST, Inc.
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

import java.io.File;
import java.io.FilenameFilter;

import javax.servlet.ServletContext;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.SharedResources;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.ContextRelativeResource;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Wicket component to add the 
 * <a href="http://java.sun.com/javase/6/docs/technotes/guides/jweb/deployment_advice.html">Sun's Deployment Toolkit</a>
 * Javascript.
 * The markup can be defined as any tag that will be replaced by the deployment script.  Attributes 
 * defined in the tag will be copied to the final applet tag by way of the deployment script.</p> 
 * 
 * <p>
 * Note: Sun's Deployment Toolkit does not work with AJAX.  If this component is rendered during
 * an ajax call, uses a standard &lt;applet&gt; tag instead of Sun's deployment.
 * </p>
 * <p>
 * Suppose we have the following markup:
 * <pre>
 * &lt;div wicket:id="applet" width=200 height=120 code="SignatureApplet.class"
 *   archive="codesign.jar"&gt;&lt;/div&gt;
 * </pre>
 * In a Wicket page we can create this component and add it to the page:
 * <pre>
 * DeployJava deployJava = new DeployJava("applet");
 * add(deployJava);
 * </pre>
 * We get the following output:
 * <pre>
 * &lt;html&gt;
 *   &lt;head&gt;
 *     &lt;script type="text/Javascript" src="http://java.com/js/deployJava.js"&gt;&lt;/script&gt;
 *   &lt;/head&gt;
 *   &lt;body&gt;
 *     &lt;script type="text/javascript"&gt;
 *          var attributes = { "width":200,"height":120,"code":"SignatureApplet.class","archive":"codesign.jar", "wicket:id":"applet"};
 *          var parameters = {};
 *          var version = null;
 *          deployJava.runApplet(attributes, parameters, version);
 *     &lt;/script&gt;
 *     &lt;applet wicket:id="applet" ... &gt;
 *          ...
 *     &lt;/applet&gt;
 *   &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * </p>
 * 
 * Adapted from Mr. Haki's <a href="http://mrhaki.blogspot.com/2009/05/wicket-component-for-java-deployment.html">Post</a>.
 */


public class DeployJava extends WebComponent implements IHeaderContributor {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(DeployJava.class);

	/**
	 * Javascript URL on Sun's website for deployJava Javascript. (={@value})
	 */
	private static final String JAVASCRIPT_URL = "http://java.com/js/deployJava.js";

	/**
	 * Attribute to set the width of the applet. (={@value})
	 */
	private static final String ATTRIBUTE_WIDTH = "width";

	/**
	 * Attribute to set the height of the applet. (={@value})
	 */
	private static final String ATTRIBUTE_HEIGHT = "height";

	/**
	 * Attribute to set the applet classname. (={@value})
	 */
	private static final String ATTRIBUTE_CODE = "code";

	/**
	 * Attribute to set the codebase for the applet. (={@value})
	 */
	private static final String ATTRIBUTE_CODEBASE = "codebase";

	/**
	 * Attribute to set the archive neede by the applet. (={@value})
	 */
	private static final String ATTRIBUTE_ARCHIVE = "archive";

	/**
	 * Minimal Java version needed for the applet.
	 */
	private String minimalVersion = "1.5";

	/**
	 * Attributes for the javaDeploy.runApplet Javascript method.
	 */
	private IValueMap appletAttributes = new ValueMap();

	/**
	 * Parameters for the javaDeploy.runApplet Javascript method.
	 */
	private IValueMap appletParameters = new ValueMap();

	/**
	 * Javascript that will be run if Java is not enabled in the browser.
	 */
	private String onJavaDisabled = "";
	
	/**
	 * Default constructor with markup id.
	 *
	 * @param id Markup id for applet.
	 */
	public DeployJava(String id) {
		super(id);
	}
	
	/**
	 * Constructor that will load a certain jar and class as an applet.  Will do its best
	 * to search for a jar file beginning with "jarName" in the /WEB-INF/lib folder
	 * and add it as a shared resource for this application, if necessary.
	 * 
	 * Usage Example:  new DeployJava("appletId", "awesome-applet", "org.example.Applet")
	 * 
	 * The above example will find /WEB-INF/lib/awesome-applet-1.0-SNAPSHOT.jar and add it
	 * as a shared resource under the DeployJava.class.
	 * 
	 * @param id
	 * @param jarName
	 * @param className
	 */
	public DeployJava(String id, String jarName, String className) {
		super(id);
		if (Application.get().getConfigurationType().equals(Application.DEVELOPMENT))
			setArchive(getSharedArchiveURL(jarName) + "?" + System.currentTimeMillis());
		else
			setArchive(getSharedArchiveURL(jarName));
		setCode(className);
	}
	
	/**
	 * Provides a URL to a shared resource for the given jarFile.  If a resource
	 * does not exist, it searches the /WEB-INF/lib folder for a file that starts with 
	 * the provided name and adds the resource first.
	 * 
	 * @param jarName
	 * @return
	 */
	public String getSharedArchiveURL(final String jarName) {
		SharedResources sr = Application.get().getSharedResources();
		
		// If this jarName is not listed as a shared resource, add it as one.
		if (sr.get(DeployJava.class, jarName, null, null, false) == null) {
			ServletContext sc = ((WebApplication)WebApplication.get()).getServletContext();
			Folder libdir = new Folder(sc.getRealPath("/WEB-INF/lib"));
			File[] options = libdir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String file) {
					return (file.startsWith(jarName));
				}
			});
			if (options == null || options.length < 1) {
				log.error("No JAR found matching {}/{}*", libdir, jarName);
			} else {
				log.debug("Adding JAR to Shared Resources: {}", options[0].getAbsolutePath());
				ContextRelativeResource resource = new ContextRelativeResource("WEB-INF/lib/" + options[0].getName());
				sr.add(DeployJava.class, jarName, null, null, resource);
			}
		}

		return urlFor(new ResourceReference(DeployJava.class, jarName)).toString();
	}

	/**
	 * Minimal Java version for the applet. E.g. Java 1.6 is "1.6"
	 * 
	 * Default minimum version is Java 1.5.
	 *
	 * @param version Minimal Java version needed by the applet.
	 */
	public void setMinimalVersion(final String version) {
		this.minimalVersion = version;
	}

	/**
	 * Width of the applet.
	 *
	 * @param width Width of the applet on screen.
	 */
	public void setWidth(final Integer width) {
		appletAttributes.put(ATTRIBUTE_WIDTH, width);
	}

	/**
	 * Height of the applet.
	 *
	 * @param height Height of the applet on screen.
	 */
	public void setHeight(final Integer height) {
		appletAttributes.put(ATTRIBUTE_HEIGHT, height);
	}

	/**
	 * Applet classname.
	 *
	 * @param code Applet classname.
	 */
	public void setCode(final String code) {
		appletAttributes.put(ATTRIBUTE_CODE, code);
	}

	/**
	 * Codebase for the applet code.
	 *
	 * @param codebase Codebase for the applet code.
	 */
	public void setCodebase(final String codebase) {
		appletAttributes.put(ATTRIBUTE_CODEBASE, codebase);
	}

	/**
	 * Archive path for the applet.
	 *
	 * @param archive Archive location for the applet.
	 */
	public void setArchive(final String archive) {
		appletAttributes.put(ATTRIBUTE_ARCHIVE, archive);
	}

	/**
	 * Add a parameter to the applet.
	 *
	 * @param key Name of the parameter.
	 * @param value Value for the parameter.
	 */
	public void addParameter(final String key, final Object value) {
		appletParameters.put(key, value);
	}
	
	/**
	 * Add an attribute to the applet tag.
	 *
	 * @param key Name of the parameter.
	 * @param value Value for the parameter.
	 */
	public void addAttribute(final String key, final Object value) {
		appletAttributes.put(key, value);
	}
	
	public void setAllowScripting(boolean b) {
		if (b)
			addAttribute("MAYSCRIPT", true);
		else
			appletAttributes.remove("MAYSCRIPT");
	}

	public String getOnJavaDisabled() {
		return onJavaDisabled;
	}

	public void setOnJavaDisabled(String onJavaDisabled) {
		this.onJavaDisabled = onJavaDisabled;
	}

	/**
	 * Get the attributes already set and assign them to the attribute
	 * list for the Javascript code (or applet tag). And we change the tag name 
	 * to "script" or "applet" based on whether this is an Ajax call.
	 *
	 * @param tag the current tag which is replaced.
	 */
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		// TODO: Get Sun and W3 to agree on some applet standards and use them here.
		final IValueMap tagAttributes = tag.getAttributes();
		appletAttributes.putAll(tagAttributes);
		tagAttributes.clear();
		if (AjaxRequestTarget.get() == null) {
			tag.setName("script");
			tagAttributes.put("type", "text/javascript");
		} else {
			tag.setName("applet");
			tag.putAll(appletAttributes);
		}
	}

	/**
	 * Create Javascript for deployJava.runApplet or applet parameters if this
	 * is an ajax call.
	 *
	 * @param markupStream MarkupStream to be replaced.
	 * @param openTag Tag we are replacing.
	 */
	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

		// If this is being directly rendered, use Sun's Deployment Toolkit.
		if (AjaxRequestTarget.get() == null) {
			final StringBuilder deployScript = new StringBuilder();
			if (appletAttributes.size() > 0) {
				deployScript.append("var attributes = {");
				for (String key : appletAttributes.keySet()) {
					deployScript.append("'" + key + "': '" + appletAttributes.get(key) +"', ");
				}
				deployScript.delete(deployScript.lastIndexOf(","), deployScript.length());
				deployScript.append("};\n");
			} else {
				deployScript.append("var attributes = {};\n");
			}
			if (appletParameters.size() > 0) {
				deployScript.append("var parameters = {");
				for (String key : appletParameters.keySet()) {
					deployScript.append("'" + key + "': '" + appletParameters.get(key) +"', ");
				}
				deployScript.delete(deployScript.lastIndexOf(","), deployScript.length());
			deployScript.append("};\n");
			} else {
				deployScript.append("var parameters = {};\n");
			}
			if (minimalVersion != null) {
				deployScript.append("var version = \"" + minimalVersion + "\";\n");
			} else {
				deployScript.append("var version = null;\n");
			}
			deployScript.append(String.format("if (navigator.javaEnabled()) { deployJava.runApplet(attributes, parameters, version); } else { %s }\n", 
					onJavaDisabled));
			replaceComponentTagBody(markupStream, openTag, deployScript.toString());
			
		// Otherwise, just add the parameters.
		} else {
			final StringBuilder parameterList = new StringBuilder();
			for (String key : appletParameters.keySet()) {
				parameterList.append("<param name='" + key + "' value='" + appletParameters.get(key) +"' />\n");
			}
			replaceComponentTagBody(markupStream, openTag, parameterList.toString());
		}
	}

	/**
	 * Add Javascript src reference in the HTML head section of the web page.
	 *
	 * @param response Header response.
	 */
	public void renderHead(IHeaderResponse response) {
		if (AjaxRequestTarget.get() == null)
			response.renderJavascriptReference(JAVASCRIPT_URL);
	}
}
