/*
 * Copyright 2011-2016 CAST, Inc.
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
package org.cast.cwm.data.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.BinaryFileData;

/**
 * Simple HTML5 audio player for BinaryFileData objects.
 *
 * MP3 data in the BFD will be served directly;
 * WAV data will be converted into mp3 for compatibility with most browsers.
 * No extra script, CSS, flash, etc needs to be loaded so this should be good for inserting into reports, etc.
 *
 * The audio element will have browser-determined styling and controls, and is set to a width of 100%,
 * so put it in a container of the width you want or use an AttributeModifier on this component.
 *
 * @author bgoldowsky
 *
 */
@Slf4j
public class Html5PlayerPanel extends GenericPanel<BinaryFileData> {

	private static final long serialVersionUID = 1L;

	public Html5PlayerPanel(String id, IModel<BinaryFileData> model) {
		super(id, model);
        boolean hasData = (model != null
                && model.getObject()!=null
                && model.getObject().getData() != null
                && model.getObject().getData().length>0);

        WebMarkupContainer message = new WebMarkupContainer("noaudio");
        add(message);

        WebMarkupContainer audio = new WebMarkupContainer("audio");
        add(audio);

        if (hasData) {
            String mimeType = model.getObject().getMimeType();
            log.debug("Mime type for player: {}", mimeType);
            if (mimeType.equals("audio/mpeg")) {
                audio.add(new Mp3AudioSource("source", model));
            } else if (mimeType.equals("audio/wav")) {
                audio.add(new ConvertedMp3AudioSource("source", model));
            } else {
                log.error("Unexpected MIME type of audio BinaryFileData: {}", mimeType);
                audio.add(new WebMarkupContainer("source"));
                hasData = false;
            }
        }

        message.setVisibilityAllowed(!hasData);
        audio.setVisibilityAllowed(hasData);

    }

}
