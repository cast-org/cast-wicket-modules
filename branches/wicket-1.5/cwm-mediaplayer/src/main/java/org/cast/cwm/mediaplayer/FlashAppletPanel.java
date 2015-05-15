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
package org.cast.cwm.mediaplayer;

import java.util.Formatter;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/** Component to render a Flash applet.
 * 
 * You specify parameters such as width, height, 
 * whether full-screen access is desired, and pass in the Flash object.
 * 
 * This uses JavaScriptFlashGateway after page load to actually 
 * insert the Flash portably.
 * 
 * Not all possible Flashvars are currently supported by this class, for instance 
 * allowscriptaccess, and background color are not but may be added in the future.
 * 
 * @author Boris Goldowsky
 *
 */
public class FlashAppletPanel extends Panel implements IHeaderContributor {
	
	public static final String WMODE_WINDOW = "window";
	public static final String WMODE_OPAQUE = "opaque";
	public static final String WMODE_TRANSPARENT = "transparent";
	
	private static final long serialVersionUID = 1L;
	
	protected int width;
	protected int height;
	protected String flashVars;
	protected boolean fullScreen = false;
	protected String wmode = null;
	protected CharSequence appletHref;
	protected String containerId;
	
	public FlashAppletPanel(final String id, int width, int height) {
		super(id);
		this.width = width;
		this.height = height;
		setOutputMarkupId(true);
		containerId = getMarkupId();
	}

	public FlashAppletPanel(final String id, ResourceReference appletrr, int width, int height, String flashVars) {
		this(id, width, height);
		this.flashVars = flashVars;
		this.setAppletResourceReference(appletrr);
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		// Enforcing a div with width/height attributes allows javascript functions to detect the dimensions
		// before the JavaScriptFlashGateway.js generates the <embed> tag.
		checkComponentTag(tag, "div");
		String style = "width:" + String.valueOf(getWidth()) + "px; height:" + String.valueOf(getHeight()) + "px;";
		tag.put("class", tag.getAttribute("class") == null ? "mediaPlaceholder" : "mediaPlaceholder" + tag.getAttribute("class"));
		tag.put("style", tag.getAttribute("style") == null ? style : style + tag.getAttribute("style"));
	}
	
	public void renderHead(IHeaderResponse r) {
		r.renderJavaScriptReference(new JavaScriptResourceReference(FlashAppletPanel.class, "JavaScriptFlashGateway.js"));
		
		Formatter f = new Formatter();
		f.format("var tag = new FlashTag('%s', %d, %d);\n", getAppletHref(), getWidth(), getHeight());
		f.format("tag.setAllowFullScreen(%b);\n", isFullScreen());
		if (wmode != null)
			f.format("tag.setMode('%s');\n", wmode);
		f.format("tag.setFlashvars('%s');\n", getFlashVars());
		f.format("tag.setId('%s');\n", containerId + "-flash");
		f.format("document.getElementById('%s').innerHTML = tag.toString();", containerId);
		r.renderOnLoadJavaScript(f.toString());
		f.close();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getFlashVars() {
		return flashVars;
	}

	public void setFlashVars(String flashVars) {
		this.flashVars = flashVars;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public String getWmode() {
		return wmode;
	}

	public void setWmode(String wmode) {
		this.wmode = wmode;
	}

	public void setAppletResourceReference (ResourceReference rr) {
		this.appletHref = urlFor(rr, null).toString();
	}

	public CharSequence getAppletHref() {
		return appletHref;
	}
	
	public void setAppletHRef (CharSequence href) {
		this.appletHref = href;
	}

}
