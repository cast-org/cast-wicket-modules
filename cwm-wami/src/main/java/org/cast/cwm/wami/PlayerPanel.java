/*
 * Copyright 2011-2019 CAST, Inc.
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

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.cast.cwm.data.UserContent;
import org.cast.cwm.service.ICwmService;
import org.wicketstuff.jslibraries.JSLib;
import org.wicketstuff.jslibraries.Library;
import org.wicketstuff.jslibraries.VersionDescriptor;

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
public class PlayerPanel<T extends UserContent> extends BaseAudioPanel<T> implements IHeaderContributor {

	@Inject
	private ICwmService cwmService;

	public static final String BINARY_FILE_DATA_MAPPER_PREFIX = "userdata";
		
	private AudioSkin audioSkin;
	
	/**
	 * The place where the SWF may be inserted.
	 */
	private WamiAppletHolder appletHolder;

	private static final long serialVersionUID = 1L;

	public PlayerPanel(String id, IModel<T> mUserContent, AudioSkin skin, WamiAppletHolder appletHolder) {
		this(id, mUserContent, skin);
		this.appletHolder = appletHolder;
	}

	public PlayerPanel(String id, IModel<T> mUserContent, AudioSkin skin) {
		super(id, mUserContent);
		this.audioSkin = skin;
		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		// If no appletHolder was specified in the constructor, search for one on the page.
		if (appletHolder == null) {
			Iterator<Component> iterator = getPage().visitChildren(WamiAppletHolder.class).iterator();
			if (iterator.hasNext())
				appletHolder = (WamiAppletHolder) iterator.next();
			else
				throw new RuntimeException("This component must be used on a page with a WamiAppletHolder");
		}
	}
	
	@Override
	public String getVariation() {
		return audioSkin.getVariationName();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		JSLib.getHeaderContribution(VersionDescriptor.alwaysLatestOfVersion(Library.SWFOBJECT, 2)).renderHead(response);

        response.renderJavaScriptReference(new PackageResourceReference(PlayerPanel.class, "audio_applet.js"));
        response.renderJavaScriptReference(new PackageResourceReference(PlayerPanel.class, "wami-recorder.js"));
        response.renderJavaScriptReference(new PackageResourceReference(PlayerPanel.class, "castrecorder.js"));
        
        if (audioSkin.isHasCss())
        	response.renderCSSReference(new PackageResourceReference(PlayerPanel.class, "audio_applet.css"));

        configureRecorder(response);
	}

	protected void configureRecorder(IHeaderResponse response) {
		
        RecorderOptions recorderOptions = getRecorderOptions();

        Gson gson = new Gson();
        response.renderOnLoadJavaScript(String.format("createCastRecorder('%s', %s);", 
        		this.getMarkupId(), gson.toJson(recorderOptions)));
	}

	protected RecorderOptions getRecorderOptions() {
		boolean hasExistingContent = getModelObject().getPrimaryFile() != null;

		PackageResourceReference wamiRR = new PackageResourceReference(PlayerPanel.class, "Wami2.swf");
		String wamiURL = urlFor(wamiRR, null).toString();

		return new RecorderOptions(
        		appletHolder.getMarkupId(),
                wamiURL,
                null,
                BINARY_FILE_DATA_MAPPER_PREFIX + "/",
                getModelObject().getId(),
                (hasExistingContent ? getModelObject().getPrimaryFile().getId() : 0) );
	}

    /**
     * Configuration information sent to the recorder javascript.
     * This is a structure to be rendered as a JSON configuration argument. 
     *
     */
	static class RecorderOptions {
		String swfId;
        String swfUrl;
        String recordUrl;
        String playPrefix;
        Long userContentId;
        Long binaryFileId;

        RecorderOptions(String swfId, String swfUrl, String recordUrl, String playPrefix, Long userContentId, Long binaryFileId) {
        	this.swfId = swfId;
            this.swfUrl = swfUrl;
            this.recordUrl = recordUrl;
            this.playPrefix = playPrefix;
            this.userContentId = userContentId;
            this.binaryFileId = binaryFileId;
        }
    }

}
