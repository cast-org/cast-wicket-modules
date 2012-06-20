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
package org.cast.cwm.mediaplayer;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.SharedResources;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Panel to display a Flash media player or rollover to an html5 media player
 * 
 * The Flash parameters set are appropriate for the JWPlayer 
 * (See http://www.longtailvideo.com/players/jw-flv-player/)
 * However this could easily be modified to support a different player.
 * 
 * JWPlayer is free for non-commercial use, but cannot legally be redistributed.
 * So it is not bundled with this component; you must put a copy in your application.
 * By default this component expects it to be in a 'swf' subdirectory of 
 * the webapp context directory (ie, in src/main/webapp/swf/player.swf).
 * Alternatively, you can pass any Resource for the player to the setPlayerResource() method.
 * 
 * @author Boris Goldowsky
 *
 */
public class MediaPlayerPanel extends Panel implements IHeaderContributor {

	protected static final long serialVersionUID = 1L;
	
	protected int width;
	protected int height;
	protected String markupId;
	protected String videoHRef = null;
	protected String previewHRef = null;
	protected String captionHRef = null;
	protected String audioDescriptionHRef = null;
	protected String skinHRef = null;
	protected CharSequence sourceHRef;
	protected boolean autostart = false;
	protected boolean showDownloadLink = false;
	protected boolean useOnPlay = false;
	protected boolean fullScreen = false;
	protected String fallbackText = "Click to watch video";
	protected String downloadText = "Download video";
	protected AbstractDefaultAjaxBehavior playResponse;
	
	protected boolean useLevels = false;
	
	protected static Resource playerResource = null;
	protected static boolean playerResourceStored = false; // has it been stored as a SharedResource?
	
	private final static Logger log = LoggerFactory.getLogger(MediaPlayerPanel.class);
	
	public MediaPlayerPanel(String id, ResourceReference video, int width, int height) {
		this(id, "", width, height);
		this.videoHRef = toSiteAbsolutePath(video);
		if (videoHRef == null)
			log.warn("Video path is null");
	}

	public MediaPlayerPanel(String id, String videoHRef, int width, int height) {
		super(id);
		this.videoHRef = isExternal(videoHRef) ? videoHRef : toSiteAbsolutePath(videoHRef);
		this.skinHRef = toSiteAbsolutePath(new ResourceReference(MediaPlayerPanel.class, "skins/five/five.xml"));
		this.width = width;
		this.height = height;
		setOutputMarkupId(true);
		markupId = getMarkupId();
		addComponents();		
	}
	
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		
		if (playerResource == null) {
			// Set default player if none other was set
			playerResource = PackageResource.get(MediaPlayerPanel.class, "player.swf");
		}
		
		// Save player as an application shared resource, 
		// so that it has a constant URL and can be cached by browsers
		if (!playerResourceStored) {
			SharedResources sr = Application.get().getSharedResources();		
			sr.add(MediaPlayerPanel.class, "player", null, null, playerResource);
			playerResourceStored = true;
			log.debug ("Stored new shared resource for video player: {}", playerResource);
		}		
	}

	
	public CharSequence getSourceHRef() {
		return urlFor(new ResourceReference(MediaPlayerPanel.class, "player"));
	}
	

	// Note Flash seems to expect filenames relative to the Flash applet, not relative to the current page.
	// So the relative path generated by urlFor is not going to work.	
	protected String toSiteAbsolutePath(String p) {
		try {
			String path = URLEncoder.encode(p != null ? p : "", "UTF-8");
			URI url = new URI(RequestUtils.toAbsolutePath(path));
			return url.getPath();

		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}

	}
	protected String toSiteAbsolutePath(ResourceReference rr) {
		if (rr == null)
			return null;
		CharSequence url = urlFor(rr);
		if (url == null) {
			log.error("Could not determine URL for media {}", rr);
			return "";
		}
		return toSiteAbsolutePath(url.toString());
	}

	
	// TODO: This is a cheap way of determining whether a source file is external.  We should probabl do something better.
	protected boolean isExternal(String url) {
		return url != null && url.startsWith("http://");
	}
	
	protected void addComponents() {

		// this is not currently used - may want to use for ipad/ipod download link
//		WebMarkupContainer player = new WebMarkupContainer("spaceholder");
//		add(player);
//		player.setOutputMarkupId(true);
//		markupId = player.getMarkupId();
		
//		ExternalLink fallbackLink = new ExternalLink("failureLink", new PropertyModel<String>(this, "videoHRef"));
//		player.add(fallbackLink);
//		fallbackLink.add(new Label("text", new PropertyModel<String>(this, "fallbackText")));
//		
//		WebMarkupContainer dl = new WebMarkupContainer("download") {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public boolean isVisible() {
//				return isShowDownloadLink();
//			}
//		};
//		add(dl);
//		ExternalLink link = new ExternalLink("link", new PropertyModel<String>(this, "videoHRef"));
//		dl.add(link);
//
//		// Button to download the video
//		link.add(new Image("button", new ResourceReference(MediaPlayerPanel.class, "download_arrow.png")));
//		link.add(new Label("text", new PropertyModel<String>(this, "downloadText")));
		
		playResponse = new AbstractDefaultAjaxBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				String status = RequestCycle.get().getRequest().getParameter("status");
				if (useOnPlay)
					onPlay(status);
			}
		};
		add(playResponse);
	}

	public void renderHead(IHeaderResponse r) {

		r.renderJavascriptReference(new ResourceReference(MediaPlayerPanel.class, "jwplayer.js"));

		StringBuffer jsString = new StringBuffer();
		jsString.append("jwplayer(" + "\"" + getMarkupId() + "\").setup({" +
				"flashplayer: " + "\"" + getSourceHRef() + "\", " +
				"file: " + "\"" + getVideoHRef() +"\", " +
				"height: " + String.valueOf(getHeight()) + ", " +
				"width: " + String.valueOf(getWidth()));

		if (!Strings.isEmpty(previewHRef))
			jsString.append(", " + "image: " + "\"" + previewHRef +  "\"");
		
		if (!Strings.isEmpty(skinHRef))
			jsString.append(", " + "skin: " + "\"" + skinHRef +  "\"");

		// if you want to change the order jwplayer uses
		//jsString.append(",  \'modes\': [ {type: \'html5\'}, {type: \'flash\', src: \'" + getSourceHRef() + "\'}, {type: \'download\'} ] ");
		
		// add the plugins section
		jsString.append(", " + "plugins: { ");

		// Disable viral plugin (there doesn't seem to be any way to avoid loading it in the current version)
		jsString.append("'viral-2': {'onpause': false, 'oncomplete': false, 'allowmenu': false, 'functions' : 'info'}");

		if (!Strings.isEmpty(captionHRef))
			jsString.append(", \"captions-2\": {file: " + "\"" + captionHRef +  "\"}");

		if (!Strings.isEmpty(audioDescriptionHRef)) {
			if (!Strings.isEmpty(captionHRef))
				jsString.append(", ");
			jsString.append("\"audiodescription-2\": {file: " + "\"" + audioDescriptionHRef +  "\"}");
		}

		jsString.append("}"); // end plugins

		jsString.append("});"); // end setup
		r.renderOnDomReadyJavascript(jsString.toString());
		
		if (useOnPlay) {
			r.renderOnDomReadyJavascript(getEventScript());
		}
	}
	
	protected String getEventScript() {
		String jsString = 
				"	var url=\'" + playResponse.getCallbackUrl(false) + "\';\n" +
				"   var status;\n" +
				"	jwplayer(" + "\"" + getMarkupId() + "\").onPlay(function() {" +
				"      if (status == 'paused') {\n " +
				"			status = 'resume'; } " +
				"	   else {" +
				"      		status = 'play';\n };" +
				"      wicketAjaxGet(url + '&status=' + status, function() {}, function() {});\n" +
				" 	});\n" +

				"	jwplayer(" + "\"" + getMarkupId() + "\").onPause(function() {" +
				"      status = 'paused';\n "  +
				"      wicketAjaxGet(url + '&status=' + status, function() {}, function() {});\n" +
				" 	});\n" +

				"	jwplayer(" + "\"" + getMarkupId() + "\").onComplete(function() {" +
				"      status = 'completed';\n "  +
				"      wicketAjaxGet(url + '&status=' + status, function() {}, function() {});\n" +
				" 	});\n";
		return jsString;		
	}

	/**
	 * Called when the video is played/paused by the user.  By default does nothing.
	 * 
	 * NOTE:  This function is useless unless you run setUseOnPlay(true).
	 * 
	 * @param playing true if the video begins playing, false if it is paused
	 */
	public void onPlay(String status) {
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

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public String getVideoHRef() {
		return videoHRef;
	}

	public void setVideoHRef(String videoHRef) {
		this.videoHRef = toSiteAbsolutePath(videoHRef);
	}

	public String getPreviewHRef() {
		return previewHRef;
	}

	public void setPreviewHRef(String previewHRef) {
		this.previewHRef = toSiteAbsolutePath(previewHRef);
	}
	
	public void setPreview(ResourceReference preview) {
		this.previewHRef = toSiteAbsolutePath(preview);
	}
	
	public void setSkinHRef(String skinHRef) {
		this.skinHRef = toSiteAbsolutePath(skinHRef);
	}
	
	public void setSkin(ResourceReference skin) {
		this.skinHRef = toSiteAbsolutePath(skin);
	}
	
	public void setSkin(String skinName) {
		this.skinHRef = toSiteAbsolutePath(new ResourceReference(MediaPlayerPanel.class, skinName));
	}
	
	public void setCaptionFile (ResourceReference captionResourceRef) {
		this.captionHRef = toSiteAbsolutePath(captionResourceRef);
	}
	
	public void setCaptionHRef (String captionHRef) {
		this.captionHRef = toSiteAbsolutePath(captionHRef);
	}

	public void setAudioDescriptionFile (ResourceReference audioDescriptionResourceRef) {
		this.audioDescriptionHRef = toSiteAbsolutePath(audioDescriptionResourceRef);
	}
	
	public void setAudioDescriptionHRef (String audioDescriptionHRef) {
		this.audioDescriptionHRef = toSiteAbsolutePath(audioDescriptionHRef);
	}

	public boolean isShowDownloadLink() {
		return showDownloadLink;
	}

	public void setShowDownloadLink(boolean showDownloadLink) {
		this.showDownloadLink = showDownloadLink;
	}

	public String getFallbackText() {
		return fallbackText;
	}

	public void setFallbackText(String fallbackText) {
		this.fallbackText = fallbackText;
	}

	public String getDownloadText() {
		return downloadText;
	}

	public void setDownloadText(String downloadText) {
		this.downloadText = downloadText;
	}

	public boolean isAutostart() {
		return autostart;
	}

	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}
	
	public void setPlayerResource (Resource playerResource) {
		MediaPlayerPanel.playerResource = playerResource;
		MediaPlayerPanel.playerResourceStored = false; // cause new resource to be saved into SharedResources
	}
	
	public boolean isUseOnPlay() {
		return useOnPlay;
	}

	/**
	 * Sets whether this panel loads javascript resources to detect click events in the video
	 * and run the onPlay(String status) function.
	 * 
	 * @param useOnPlay - true if enabled, false otherwise
	 */
	public void setUseOnPlay(boolean useOnPlay) {
		this.useOnPlay = useOnPlay;
	}
}