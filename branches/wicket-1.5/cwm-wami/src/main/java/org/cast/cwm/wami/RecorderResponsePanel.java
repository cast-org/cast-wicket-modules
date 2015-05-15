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

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.util.io.IOUtils;
import org.cast.cwm.data.Response;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.IResponseService;

import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * An audio recorder and player, based on @Response rather than UserContent.
 * 
 * Important notes:
 *    1. this requires that a {@link org.cast.cwm.BinaryFileDataMapper} be set up with prefix {@link #BINARY_FILE_DATA_MAPPER_PREFIX}.
 *    2. The markup provided with this Panel is pretty basic and no CSS is supplied. You should probably subclass this panel
 *       and provide better markup.
 *    3. JQuery is assumed to be already loaded on the page.
 *    
 * @author bgoldowsky
 *
 */
@Slf4j
public class RecorderResponsePanel extends PlayerResponsePanel<Response> implements IHeaderContributor {

	@Inject
	private ICwmService cwmService;
	
	@Inject
	protected IResponseService responseService;

	private AbstractAjaxBehavior listener;
	
	private String pageName;
	
	private static final long serialVersionUID = 1L;

	public RecorderResponsePanel(String id, IModel<Response> mUserContent, AudioSkin audioSkin, String pageName) {
		super(id, mUserContent, audioSkin);
		listener = new AudioDataListenerBehavior();
		this.pageName = pageName;
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
    	if (req.getContentLength() > 50) {
    		responseService.saveBinaryResponse(getModel(), audioData(req), "audio/wav", "audio", pageName);
    		log.debug("Audio callback received, length {}; saved BFD for Response {}",
    				req.getContentLength(),
    				getModelObject().getId());
    	} else {
    		log.warn("Audio callback received, length {} for Response {}: ignoring apparently empty audio!",
    				req.getContentLength(),
    				getModelObject().getId());
    	}
    }

	/**
	 * Format incoming audio data into the database object.
	 */
	protected byte[] audioData (HttpServletRequest req) {
		try {
			return IOUtils.toByteArray(req.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException("Failed to save audio data");
		}
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
	    private void sendResponse(final RequestCycle requestCycle, Response response) {
	        RecordingResponse recordingResponse = new RecordingResponse(response.getResponseData().getBinaryFileData().getId());
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

        RecordingResponse(Long binaryFileId) {
            this.userContentId = null;
            this.binaryFileId = binaryFileId;
        }
    }

}
