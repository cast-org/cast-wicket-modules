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
package org.cast.cwm.tag.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;
import org.cast.cwm.tag.model.Tag;

/**
 * A label that displays the appropriate string for a given tag.  Useful
 * when displaying '*' in a different font.
 * 
 * @author jbrookover
 *
 */
public class TagLabel extends Label {
	
	private static final long serialVersionUID = 1L;

	public TagLabel(String id, Tag tag) {
		super(id, new Model<Tag>(tag));
			
	}
	
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		Tag tag = (Tag)getDefaultModelObject();
		String tagname = "";
		if (tag != null) {
			tagname = tag.getName();
			String userAgent = WebSession.get().getClientInfo().getUserAgent();
			if (userAgent != null && userAgent.toLowerCase().contains("win")) // Windows doesn't support Unicode out of the box
				if (userAgent.toLowerCase().contains("ie")) {
					tagname = tagname.replace("*", "<span style='font-family:Wingdings'>&#171;</span>"); // If we're in IE, use Wingdings!

				} else {
					; // Otherwise, leave as asterixssessxss (sp?)
				}
			else
				tagname = tagname.replace('*', '\u2605'); // We're not in Windows, so we can probably do Unicode
		}
		replaceComponentTagBody(markupStream, openTag, tagname);
	}

	@Override
	public boolean isVisible() {
		return (getDefaultModelObject() != null);
	}
	
}
