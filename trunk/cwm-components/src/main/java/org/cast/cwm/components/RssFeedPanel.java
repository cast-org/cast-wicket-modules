package org.cast.cwm.components;

import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.wicketstuff.rome.SyndEntryListModel;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 * General purpose RSS Feed Panel.  Provide the RSS Feed URL and the max
 * number of items from the feed to display.
 *
 */
public class RssFeedPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public RssFeedPanel (String wicketId, String url, int maxItems) {
		super(wicketId);
		
		List<? extends SyndEntry> entryList = new SyndEntryListModel(url).getObject();
		entryList = entryList.subList(0, Math.min(entryList.size(), maxItems));
		
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