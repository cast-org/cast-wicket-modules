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
package org.cast.audioapplet.component;

import java.io.OutputStream;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.cast.cwm.components.DeployJava;
import org.cast.cwm.components.service.JavascriptService;

/**
 * A component that holds the Java audio applet, and HTML to communicate with it for playing audio.
 * If recording functionality is also needed, use AbstractAudioRecorder instead.
 */
public class AudioPlayer extends Panel implements IHeaderContributor {
	
	private static final long serialVersionUID = 1L;
	
	protected boolean autoPlay = false;
	
	protected DeployJava dj;

	/** 
	 * Create a new Audio Applet that does not autoplay
	 * 
	 * @param id the Wicket ID of the component
	 * @param model the audio applet model
	 */
	public AudioPlayer(String id, IAudioAppletModel model) {
		this(id, model, false);
	}
	
	/** 
	 * Create a new Audio Applet in the page.
	 * 
	 * @param id the Wicket ID of the component
	 * @param model the audio applet model 
	 * @param autoPlay true if the audio should begin playing back immediately on page load
	 */
	public AudioPlayer(String id, IAudioAppletModel model, boolean autoPlay) {
		super(id, model);
		this.autoPlay = autoPlay;
	}
	
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
		addJavaDeployer("audioapplet");
		addButtons();
	}

	protected void addJavaDeployer(String wicketId) {
		dj = new DeployJava(wicketId, "audioapplet", "org.cast.audioapplet.RecorderApplet");
		dj.setOutputMarkupId(true);
		dj.setWidth(1);
		dj.setHeight(1);
		dj.setAllowScripting(true);		
		dj.addParameter("readOnly", "true");
		if (autoPlay) 
			dj.addParameter("autoPlay", "true");
		add(dj);
	}
	
	protected void addButtons() {
		WebMarkupContainer play = new WebMarkupContainer("playButton");
		play.add(new SimpleAttributeModifier("onclick", "play('" + dj.getMarkupId() + "')"));
		add(play);
		
		WebMarkupContainer pause = new WebMarkupContainer("pauseButton");
		pause.add(new SimpleAttributeModifier("onclick", "pause('" + dj.getMarkupId() + "')"));
		add(pause);
		
		WebMarkupContainer stop = new WebMarkupContainer("stopButton");
		stop.add(new SimpleAttributeModifier("onclick", "stop('" + dj.getMarkupId() + "')"));
		add(stop);
	}

	@Override
	public void onBeforeRender() {		
		if (hasData()) {
			dj.addParameter("loadExistingAudioFile", "true");
			dj.addParameter("loadURL", getDataUrl());
		}

		dj.addParameter("markupId", getMarkupId());
		
		super.onBeforeRender();
	}
	
	/**
	 * Determine whether this applet has audio data ready to be played.
	 * @return true if there is playable audio
	 */
	protected boolean hasData() {
		byte[] bytes = getModelObject(); 
		return (bytes != null && bytes.length > 0);
	}
	
	/**
	 * The URL from which initial audio data can be loaded.
	 * @return the URL
	 */
	protected CharSequence getDataUrl() {
		AudioFileLoadAjaxBehavior loadBehavior = new AudioFileLoadAjaxBehavior();
		dj.add(loadBehavior);
		return (loadBehavior.getCallbackUrl());
	}
	
	/**
	 * Return the markup id of the applet itself.  Useful for javascript, etc.
	 * @return
	 */
	public String getAppletMarkupId() {
		return dj.getMarkupId();
	}
	/**
	 * Return javascript to call the applet's basic message-passing function.
	 * @param message: must be one understood by the applet 
	 * @return Javascript expression
	 * FIXME: should perhaps use an enum of known commands
	 */
	public String generateJavascriptMessage(String message) {
		return String.format("document.getElementById('%s').messageFromJavascript('%s');", 
				getAppletMarkupId(), message);
	}

	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference(AudioPlayer.class, "audio_applet.css"));
		JavascriptService.get().includeJQuery(response);
		response.renderJavascriptReference(new ResourceReference(AudioPlayer.class, "audio_applet.js"));
	}

	class AudioFileLoadAjaxBehavior extends AbstractAjaxBehavior {

		private static final long serialVersionUID = 1L;

		public void onRequest() {
			RequestCycle.get().setRequestTarget(new IRequestTarget() {

				public void detach(RequestCycle requestCycle) { }

				public void respond(RequestCycle requestCycle) {
					try {
						OutputStream os = requestCycle.getOriginalResponse().getOutputStream();
						os.write(getModelObject());
						os.flush();
						os.close();
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Gets model
	 * 
	 * @return model
	 */
	public final IAudioAppletModel getModel()
	{
		return (IAudioAppletModel) getDefaultModel();
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public final void setModel(IAudioAppletModel model)
	{
		setDefaultModel(model);
	}

	/**
	 * Gets model object
	 * 
	 * @return model object
	 */
	public final byte[] getModelObject()
	{
		return (byte[]) getDefaultModelObject();
	}

	/**
	 * Sets model object
	 * 
	 * @param object
	 */
	public final void setModelObject(byte[] object)
	{
		setDefaultModelObject(object);
	}

}
