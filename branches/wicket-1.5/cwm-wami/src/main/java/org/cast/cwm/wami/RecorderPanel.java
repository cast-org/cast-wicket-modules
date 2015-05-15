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
package org.cast.cwm.wami;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.util.io.IOUtils;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.UserContent;
import org.cast.cwm.service.ICwmService;

import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * An audio recorder and player.
 * 
 * Important notes:
 *    1. this requires that a {@link org.cast.cwm.BinaryFileDataMapper} be set up with prefix {@link #BINARY_FILE_DATA_MAPPER_PREFIX}.
 *    2. The markup provided with this Panel is pretty basic and no CSS is supplied. You should probably subclass this panel
 *       and provide better markup.
 *    3. JQuery is assumed to be already loaded on the page.
 *    
 * @author cdietz
 * @author bgoldowsky
 *
 */
@Slf4j
public class RecorderPanel<T extends UserContent> extends PlayerPanel<T> implements IHeaderContributor {

	@Inject
	private ICwmService cwmService;
	
	private AbstractAjaxBehavior listener;
	
	private static final long serialVersionUID = 1L;

	public RecorderPanel(String id, IModel<T> mUserContent, AudioSkin skin, WamiAppletHolder appletHolder) {
		super(id, mUserContent, skin, appletHolder);
		listener = new AudioDataListenerBehavior();
		add(listener);
	}

	public RecorderPanel(String id, IModel<T> mUserContent, AudioSkin skin) {
		super(id, mUserContent, skin);
		listener = new AudioDataListenerBehavior();
		add(listener);
	}

	@Override
	protected RecorderOptions getRecorderOptions() {
		RecorderOptions options = super.getRecorderOptions();
		options.recordUrl = listener.getCallbackUrl().toString();
		return options;
	}

	/**
	 * Save the data from an incoming HTTP request into the database.
	 * @param req the HttpServletRequest callback from the WAMI recorder.
	 */
    protected void saveAudioData(HttpServletRequest req) {
		BinaryFileData audioData = audioData(req);
		cwmService.save(audioData);
		getModelObject().setPrimaryFile(audioData);
		if (getModelObject().isTransient()) {
			// The UserContent object might not be saved yet.  If not, save it and adjust model accordingly.
			cwmService.save(getModelObject());
			if (getModel() instanceof HibernateObjectModel)
				((HibernateObjectModel<T>)getModel()).checkBinding();
		}
		cwmService.flushChanges();
		log.debug("Audio callback received, length {}; saved BFD {} for UserContent {}",
				req.getContentLength(),
				audioData.getId(), getModelObject().getId());
	}

	/**
	 * Format incoming audio data into the database object.
	 */
	protected BinaryFileData audioData (HttpServletRequest req) {
		byte[] bytes;
		try {
			bytes = IOUtils.toByteArray(req.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException("Failed to save audio data");
		}
		return new BinaryFileData("audio", "audio/wav", bytes);
	}

	
	protected class AudioDataListenerBehavior extends AbstractAjaxBehavior {
		private static final long serialVersionUID = 1L;

		public void onRequest() {
			Request request = getRequest();
		    RequestCycle requestCycle = RequestCycle.get();
			HttpServletRequest req = ((ServletWebRequest)request).getContainerRequest();

			// Save into DB
			saveAudioData(req);
		    sendResponse(requestCycle, getModelObject());
		}

		// send the JSON response to the audio applet...
	    private void sendResponse(final RequestCycle requestCycle, UserContent userContent) {
	        RecordingResponse recordingResponse = new RecordingResponse(userContent.getId(),userContent.getPrimaryFile().getId());
	        Gson gson = new Gson();
	        requestCycle.scheduleRequestHandlerAfterCurrent(new TextRequestHandler("application/json", "UTF-8", gson.toJson(recordingResponse)));
	    }
		
	}
	
	/**
	 * The response information sent back to the recorder javascript after a save.
	 * This is a structure that will be rendered as JSON.
	 *
	 */
    static class RecordingResponse {
        Long userContentId;
        Long binaryFileId;

        RecordingResponse(Long userContentId, Long binaryFileId) {
            this.userContentId = userContentId;
            this.binaryFileId = binaryFileId;
        }
    }

}
