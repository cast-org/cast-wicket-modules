package org.cast.cwm.wami;

import com.google.gson.Gson;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.UserContent;
import org.cast.cwm.service.ICwmService;
import org.wicketstuff.jslibraries.JSLib;
import org.wicketstuff.jslibraries.Library;
import org.wicketstuff.jslibraries.VersionDescriptor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
public class RecorderPanel<T extends UserContent> extends GenericPanel<T> implements IHeaderContributor {

	@Inject
	private ICwmService cwmService;

	public static final String BINARY_FILE_DATA_MAPPER_PREFIX = "userdata";
	
	private AbstractAjaxBehavior listener;

	private static final long serialVersionUID = 1L;

	public RecorderPanel(String id, IModel<T> mUserContent) {
		super(id, mUserContent);
		listener = new AudioDataListenerBehavior();
		add(listener);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		JSLib.getHeaderContribution(VersionDescriptor.alwaysLatestOfVersion(Library.SWFOBJECT, 2)).renderHead(response);

//        response.renderCSSReference(new PackageResourceReference(CastRecorderPanel.class, "normalize.css"));
//        response.renderCSSReference(new PackageResourceReference(CastRecorderPanel.class, "edit.css"));

        response.renderJavaScriptReference(new PackageResourceReference(RecorderPanel.class, "audio_applet.js"));
        response.renderJavaScriptReference(new PackageResourceReference(RecorderPanel.class, "wami-recorder.js"));
        response.renderJavaScriptReference(new PackageResourceReference(RecorderPanel.class, "castrecorder.js"));

        configureRecorder(response);
	}

	protected void configureRecorder(IHeaderResponse response) {
		boolean hasExistingContent = getModelObject().getPrimaryFile() != null;
		
		PackageResourceReference wamiRR = new PackageResourceReference(RecorderPanel.class, "Wami2.swf");
		String wamiURL = urlFor(wamiRR, null).toString();

        RecorderOptions recorderOptions = new RecorderOptions(
                wamiURL,
                listener.getCallbackUrl().toString(),
                BINARY_FILE_DATA_MAPPER_PREFIX + "/",
                getModelObject().getId(),
                (hasExistingContent ? getModelObject().getPrimaryFile().getId() : 0) );

        Gson gson = new Gson();
        response.renderOnLoadJavaScript("CastRecorder.setupRecorder( " + gson.toJson(recorderOptions) +  " );" );
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
     * Configuration information sent to the recorder javascript.
     * This is a structure to be rendered as a JSON configuration argument. 
     *
     */
	static class RecorderOptions {
        String swfUrl;
        String recordUrl;
        String playPrefix;
        Long userContentId;
        Long binaryFileId;

        RecorderOptions(String swfUrl, String recordUrl, String playPrefix, Long userContentId, Long binaryFileId) {
            this.swfUrl = swfUrl;
            this.recordUrl = recordUrl;
            this.playPrefix = playPrefix;
            this.userContentId = userContentId;
            this.binaryFileId = binaryFileId;
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