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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.resource.UploadedFileResourceReference;

/**
 * Simple audio player for WAV based on HTML5 <audio> element.
 * No extra script, flash, etc needs to be loaded so this should be good for inserting into reports, etc.
 * Currently <audio> with WAV content is not supported by Internet Explorer, however.
 * The audio element will have browser-determined styling and controls, and is set to a width of 100%,
 * so put it in a container of the width you want (eg with an AttributeModifier on this component).
 * 
 * TODO: consider showing a message in the null-model case (we're seeing this sometimes in logs).
 * TODO: consider using attributes to control whether audio content is loaded eagerly or lazily.
 * 
 * @author bgoldowsky
 *
 */
public class Html5PlayerPanel extends GenericPanel<BinaryFileData> {

	private static final long serialVersionUID = 1L;

	public Html5PlayerPanel(String id, IModel<BinaryFileData> model) {
		super(id, model);
		
		// Determine URL for audio
		PageParameters pp = new PageParameters();
		if (model != null && model.getObject() != null)
			pp.add("id", model.getObject().getId());
		CharSequence url = getRequestCycle().urlFor(new UploadedFileResourceReference(), pp);
		
		WebMarkupContainer source = new WebMarkupContainer("source");
		source.add(AttributeModifier.replace("src", url.toString()));
		add(source);
	}

}
