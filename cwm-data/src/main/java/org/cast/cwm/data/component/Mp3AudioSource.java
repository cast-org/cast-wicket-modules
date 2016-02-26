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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.resource.UploadedFileResourceReference;

/**
 * Component to use on an HTML5 "source" element to send mp3 response data.
 *
 */
public class Mp3AudioSource extends WebMarkupContainer {

    public Mp3AudioSource(String id, IModel<BinaryFileData> model) {
        super(id, model);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        checkComponentTag(tag, "source");

        tag.put("type", "audio/mpeg");

        // Determine URL for audio
        PageParameters pp = new PageParameters();
        if (getModel() != null && getModel().getObject() != null)
            pp.add("id", getModel().getObject().getId());
        CharSequence url = getRequestCycle().urlFor(new UploadedFileResourceReference(), pp);
        tag.put("src", url);
    }

    @SuppressWarnings("unchecked")
    public IModel<BinaryFileData> getModel() {
        return (IModel<BinaryFileData>) getDefaultModel();
    }

}
