/*
 * Copyright 2011-2014 CAST, Inc.
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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.cast.cwm.data.Response;
import org.cast.cwm.service.ICwmService;
import org.wicketstuff.jslibraries.JSLib;
import org.wicketstuff.jslibraries.Library;
import org.wicketstuff.jslibraries.VersionDescriptor;

import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * A panel for the WAMI audio player, based on @Response rather than UserContent.
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
public class PlayerResponsePanel<T extends Response> extends BaseAudioPanel<T> implements IHeaderContributor {

	@Inject
	private ICwmService cwmService;

	private AudioSkin audioSkin;

	public static final String BINARY_FILE_DATA_MAPPER_PREFIX = "userdata";
		
	private static final long serialVersionUID = 1L;

	public PlayerResponsePanel(String id, IModel<T> mResponse, AudioSkin audioSkin) {
		super(id, mResponse);
		this.audioSkin = audioSkin;
		setOutputMarkupId(true);
	}

	@Override
	public String getVariation() {
		return audioSkin.getVariationName();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		JSLib.getHeaderContribution(VersionDescriptor.alwaysLatestOfVersion(Library.SWFOBJECT, 2)).renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(PlayerResponsePanel.class, "audio_applet.js", null, null, getVariation())));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(PlayerResponsePanel.class, "wami-recorder.js", null, null, getVariation())));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(PlayerResponsePanel.class, "castrecorder.js", null, null, getVariation())));

        if (audioSkin.isHasCss())
        	response.render(CssHeaderItem.forReference(new PackageResourceReference(PlayerResponsePanel.class, "audio_applet.css", null, null, getVariation())));

        configureRecorder(response);
	}

	protected void configureRecorder(IHeaderResponse response) {
		
        RecorderOptions recorderOptions = getRecorderOptions();

        Gson gson = new Gson();
        response.render(OnLoadHeaderItem.forScript(String.format("createCastRecorder('%s', %s);", 
        		this.getMarkupId(), gson.toJson(recorderOptions))));
	}

	protected RecorderOptions getRecorderOptions() {
		boolean hasExistingContent = getModelObject().getResponseData() != null 
				&& getModelObject().getResponseData().getBinaryFileData() != null;

		PackageResourceReference wamiRR = new PackageResourceReference(PlayerResponsePanel.class, "Wami2.swf");
		String wamiURL = urlFor(wamiRR, null).toString();

		return new RecorderOptions(
                wamiURL,
                null,
                BINARY_FILE_DATA_MAPPER_PREFIX + "/",
                null, // FIXME?  would be UserContent ID
                (hasExistingContent ? getModelObject().getResponseData().getBinaryFileData().getId() : 0) );
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

}
