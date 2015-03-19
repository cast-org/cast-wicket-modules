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
package org.cast.cwm.data.component;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.BinaryFileData;

/**
 * Simple HTML5 audio player for BinaryFileData objects.
 * Converts WAV data in the BFD into mp3 for compatibility with most browsers.
 * No extra script, flash, etc needs to be loaded so this should be good for inserting into reports, etc.
 *
 * The audio element will have browser-determined styling and controls, and is set to a width of 100%,
 * so put it in a container of the width you want or use an AttributeModifier on this component.
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
		add(new Mp3AudioSource("source", model));
	}

}
