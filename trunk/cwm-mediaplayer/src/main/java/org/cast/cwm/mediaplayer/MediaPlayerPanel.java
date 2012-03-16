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
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Panel to display a Flash media player.
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
public class MediaPlayerPanel extends FlashAppletPanel {

	protected static final long serialVersionUID = 1L;
	
	protected boolean showDownloadLink = false;
	protected String videoHRef = null;
	protected String previewHRef = null;
	protected String skinHRef = null;
	protected String captionHRef = null;
	protected String audioDescriptionHRef = null;
	protected boolean autostart = false;
	protected String fallbackText = "Click to watch video";
	protected String downloadText = "Download video";
	protected AbstractDefaultAjaxBehavior playResponse;
	protected boolean useOnPlay = false;
	
	protected static Resource playerResource = null;
	protected static boolean playerResourceStored = false; // has it been stored as a SharedResource?
	
	private final static Logger log = LoggerFactory.getLogger(FlashAppletPanel.class);
	
	public MediaPlayerPanel(String id, ResourceReference video, int width, int height) {
		this(id, "", width, height);
		this.videoHRef = toSiteAbsolutePath(video);
		if (videoHRef == null)
			log.warn("Video path is null");
	}

	public MediaPlayerPanel(String id, String videoHRef, int width, int height) {
		super(id, width, height);
		this.setFullScreen(true);
		this.setWmode(WMODE_OPAQUE);
		this.videoHRef = isExternal(videoHRef) ? videoHRef : toSiteAbsolutePath(videoHRef);
		addComponents();
	}
	
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		
		if (playerResource == null) {
			// Set default player if none other was set
			playerResource = PackageResource.get(MediaPlayerPanel.class, "jwplayer.swf");
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

	
	@Override
	public CharSequence getAppletHref() {
		return urlFor(new ResourceReference(MediaPlayerPanel.class, "player"));
	}
	
	@Override
	public String getFlashVars() {
		StringBuffer fv = new StringBuffer("");
		StringBuffer plugins = new StringBuffer("");
		
		if (!Strings.isEmpty(super.getFlashVars()))
			fv.append(super.getFlashVars() + "&");
		fv.append("useExternalInterface=true&");
		if (!Strings.isEmpty(videoHRef))
			fv.append("file=" + videoHRef + "&");
		if (!Strings.isEmpty(previewHRef))
			fv.append("image=" + previewHRef + "&");
		if (!Strings.isEmpty(skinHRef))
			fv.append("skin=" + skinHRef + "&");
		if (autostart)
			fv.append("autostart=true&");
		if (captionHRef != null) {
			plugins.append("captions-2,");
			fv.append("captions.back=true&captions.file=" + captionHRef + "&");
		}
		if (audioDescriptionHRef != null) {
			plugins.append("audiodescription-2,");
			fv.append("audiodescription.state=false&audiodescription.ducking=true&audiodescription.file=" + audioDescriptionHRef + "&");
			// For testing purposes, you can add "audiodescription.debug=true" to the above
		}
		if (plugins.length() > 0) {
			// Avoid trailing comma
			if (plugins.charAt(plugins.length()-1) == ',')
				plugins.deleteCharAt(plugins.length()-1);
			fv.append("plugins=").append(plugins).append("&");
		}
		fv.append("id=" + containerId + "-flash&");	//needed for playerReady() 
		return fv.toString();
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

		WebMarkupContainer player = new WebMarkupContainer("spaceholder");
		add(player);
		player.setOutputMarkupId(true);
		containerId = player.getMarkupId();
		
		ExternalLink fallbackLink = new ExternalLink("failureLink", new PropertyModel<String>(this, "videoHRef"));
		player.add(fallbackLink);
		fallbackLink.add(new Label("text", new PropertyModel<String>(this, "fallbackText")));
		
		WebMarkupContainer dl = new WebMarkupContainer("download") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return isShowDownloadLink();
			}
		};
		add(dl);
		ExternalLink link = new ExternalLink("link", new PropertyModel<String>(this, "videoHRef"));
		dl.add(link);

		// Button to download the video
		link.add(new Image("button", new ResourceReference(MediaPlayerPanel.class, "download_arrow.png")));
		link.add(new Label("text", new PropertyModel<String>(this, "downloadText")));
		
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
		if (useOnPlay) {
			String javascript = 
				"\nvar playing = false;\n" + 
				"var url='" + playResponse.getCallbackUrl(false) + "';\n" +
				"var player;\n\n" +

				"function onPlay (obj) {\n" +
				"     playing = !playing;\n" +
				"     var status;\n" +
				"     if(player.getConfig().state == 'IDLE') {\n" +
				"          status = 'play';\n" +
				"     } else if (player.getConfig().state == 'PAUSED') {\n" +
				"          status = 'resume';\n" +
				"     } else if (player.getConfig().state == 'PLAYING') {\n"+
				"          status = 'pause';\n" +
				"     } else if (player.getConfig().state == 'COMPLETED') {\n" +
				"          status = 'replay';\n" +
				"     }\n" +
				"     var wcall = wicketAjaxGet(url + '&status=' + status, function() {}, function() {});\n" +
				"}\n\n" +

				"function playerReady(obj) {\n\n" +
				"     var id = obj['id'];\n" +
				// The FlastTag generator uses <object id=...> for IE and <embed name=...> for Others
				"     player = $('object[id=' + id + '], embed[name=' + id + ']').get(0);\n" +
				"     player.addViewListener('PLAY', 'onPlay');\n" +
				"}\n";

			r.renderJavascript(javascript, "VideoPlayer");
		}
		super.renderHead(r);
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
