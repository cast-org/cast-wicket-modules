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
package org.cast.cwm.components;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.rome.SyndEntryListModel;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 * General purpose RSS Feed Panel.  Provide the RSS Feed URL and the max
 * number of items from the feed to display.
 *
 */

public class RssFeedPanel extends Panel {
	
	private static final Logger log = LoggerFactory.getLogger(RssFeedPanel.class);
	private static final long serialVersionUID = 1L;

	public RssFeedPanel (String wicketId, String url, int maxItems) {
		super(wicketId);
		
		List<? extends SyndEntry> entryList;
		try {
			 entryList = new SyndEntryListModel(url).getObject();
			 entryList = entryList.subList(0, Math.min(entryList.size(), maxItems));
		} catch (RuntimeException e) {
			// SyndEntryListModel throws a RuntimeException if it can't connect to the given URL.
			log.error("Failed to get RSS feed from {}: {}", url, e.getMessage());
			entryList = Collections.emptyList();
		}
		
		add(new ListView<SyndEntry> ("item", entryList) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<SyndEntry> item) {
				SyndEntry entry = item.getModelObject();
				ExternalLink link = new ExternalLink("link", entry.getLink());
				item.add(link);
				link.add(new Label("title", entry.getTitle()));
				item.add(new ApproximateDateLabel("date", new PropertyModel<Date>(entry, "publishedDate")));
			}
			
		});
	}
}