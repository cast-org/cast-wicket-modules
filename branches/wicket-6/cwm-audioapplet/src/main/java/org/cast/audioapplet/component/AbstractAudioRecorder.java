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
package org.cast.audioapplet.component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IDetachable;

/**
 * A component that holds the Java audio-recording applet, and record/play/pause/stop buttons
 * that communicate with it.  The model will be used for the initial audio clip loaded into the applet.
 * Abstract because there is no standard method for saving audio data.  You must extend this class and 
 * implement the onReceiveAudio and onSave methods.
 *  
 * The {@link #generateStandardSaveLink(String)} method can be used to create a Link component
 * for initiating the saving the audio back to the server.
 *
 */
public abstract class AbstractAudioRecorder extends AudioPlayer implements IHeaderContributor {
	
	private static final long serialVersionUID = 1L;
	
	protected boolean readOnly = false;
	
	private AbstractDefaultAjaxBehavior notifyBehavior;

	/** 
	 * Create a new Audio Applet that does not autoplay
	 * 
	 * @param id the Wicket ID of the component
	 * @param model the audio applet model
	 */
	public AbstractAudioRecorder(String id, IAudioAppletModel model) {
		this(id, model, false);
	}
	
	/** 
	 * Create a new Audio Applet in the page.
	 * 
	 * @param id the Wicket ID of the component
	 * @param model the audio applet model 
	 * @param autoPlay true if the audio should begin playing back immediately on page load
	 */
	public AbstractAudioRecorder(String id, IAudioAppletModel model, boolean autoPlay) {
		super(id, model, autoPlay);
		this.readOnly = model!=null && model.isReadOnly();
	}
	
	protected void addJavaDeployer(String wicketId) {
		super.addJavaDeployer(wicketId);

		// Some additional parameters for recording
		dj.addParameter("readOnly", "false");
		if (getModel() != null && getModel().getMaxLength() != -1) 
			dj.addParameter("maxLength", getModel().getMaxLength());

		notifyBehavior = new AbstractDefaultAjaxBehavior() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void respond(AjaxRequestTarget target) {
				onSave(target);
			}
		};
		dj.add(notifyBehavior);
	}
	
	protected void addButtons() {
		super.addButtons();
		WebMarkupContainer record = new WebMarkupContainer("recordButton");
		if(!readOnly) {
			record.add(new SimpleAttributeModifier("onclick", "audioRecord('" + dj.getMarkupId() + "'); return false;"));
		}
		add(record);
	}

	@Override
	public void onBeforeRender() {
		AudioFileSaveAjaxBehavior saveBehavior = new AudioFileSaveAjaxBehavior();
		dj.add(saveBehavior);
		dj.addParameter("saveURL", saveBehavior.getCallbackUrl());
		super.onBeforeRender();
	}
	
	/**
	 * Generate a save link that will trigger a save on the applet 
	 * enclosed in this component.  After stopping any ongoing recording
	 * or playback, it calls the SAVE method on the applet.  This, in turn,
	 * will assemble the data and send it to the server which pushes it into
	 * this component's model, and calls onReceiveAudio().  After that request 
	 * sucessfully completes, onSave() will be invoked.  Both these methods are
	 * abstract so an extending class should define how and where the audio
	 * data is to be stored.
	 * 
	 * @param id wicket:id of the link
	 * @return Link component.
	 */
	public WebMarkupContainer generateStandardSaveLink(String id) {

		WebMarkupContainer l = new WebMarkupContainer(id) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(final ComponentTag tag) {
				super.onComponentTag(tag);

				if (isEnabled()) {
					tag.put("onclick", 
							String.format("audioStop('%s'); document.getElementById('%s').messageFromJavascript('SAVE'); return false;",
									getAppletMarkupId(), getAppletMarkupId()));
				} else {
					tag.put("onclick", "return false;");
				}
			}	  
		};

		return l;
	}
	
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.renderJavascript("function save_" + getMarkupId() + "() {" +
				"wicketAjaxGet('" + notifyBehavior.getCallbackUrl() + "'); }", 
				"save_" + getMarkupId());
	}

	class AudioFileSaveAjaxBehavior extends AbstractAjaxBehavior {

		private static final long serialVersionUID = 1L;

		public void onRequest() {
			RequestCycle.get().setRequestTarget(new IRequestTarget() {

				public void detach(RequestCycle requestCycle) { }

				public void respond(RequestCycle requestCycle) {
					HttpServletRequest r = getWebRequest().getHttpServletRequest();
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						InputStream is = r.getInputStream();
						byte[] transfer = new byte[1024];
						int amount = 0;
						while((amount = is.read(transfer)) > 0) {
							baos.write(transfer, 0, amount);
						}

						setModelObject(baos.toByteArray());
						onReceiveAudio();

					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * This method is called immediately after recorded audio data is
	 * received and stored via setModelObject().  Since this request
	 * ends before {@link AbstractAudioRecorder#onSave(AjaxRequestTarget)} is
	 * called, you must persist the audio data in this step if you are using
	 * a {@link IDetachable} model.
	 * 
	 */
	public abstract void onReceiveAudio();
	
	/**
	 * Override this method to provide ajax behavior responses
	 * to the page after a recording has been saved.  This method is
	 * called after {@link AbstractAudioRecorder#onReceiveAudio()}, but
	 * in a different request so the model will have been detached.
	 * 
	 * @param target
	 */
	public abstract void onSave(AjaxRequestTarget target);

}
