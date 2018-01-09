/*
 * Copyright 2011-2018 CAST, Inc.
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

import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackBorder;

public class FeedbackBorder extends Border {

	protected boolean hasErrors = false;

	public FeedbackBorder(String id) {
		super(id);		
		setOutputMarkupId(true);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		if (hasErrors) {
			String oldClass = tag.getAttribute("class")==null ? "" : tag.getAttribute("class")+" ";
			tag.put("class", oldClass+"has-danger");
		}
	}

	/**
	 * Update the 'visible' flag to indicate the existence (or lack thereof) of feedback messages
	 */
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		// Get the messages for the current page
		hasErrors = new FeedbackCollector(getPage()).collect(getMessagesFilter()).size() > 0;
	}

	/**
	 * @return Let subclass specify some other filter
	 */
	protected IFeedbackMessageFilter getMessagesFilter()
	{
		return new ContainerFeedbackMessageFilter(this);
	}
}
