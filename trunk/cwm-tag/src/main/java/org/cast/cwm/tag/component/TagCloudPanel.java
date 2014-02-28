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
package org.cast.cwm.tag.component;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.User;
import org.cast.cwm.tag.ITagLinkBuilder;
import org.cast.cwm.tag.TagService;
import org.cast.cwm.tag.model.Tag;
import org.cast.cwm.tag.model.TagPlusInt;
import org.cast.cwm.tag.model.TagPlusIntFrequencyComparator;
import org.cast.cwm.tag.model.TagPlusIntNameComparator;

/**
 * A standard panel that displays a sorted, clickable interface to a user's tags for 
 * this application, either in cloud or list view.  
 * 
 * @author jbrookover
 *
 */
public class TagCloudPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	protected Tag selected;
	protected List<TagPlusInt> tagList; // TODO: This shouldn't be here
	protected int maxFreq;
	protected final ITagLinkBuilder tagLinkBuilder;
	protected PageParameters additionalParameters = null;
	
	// Maximum and minimum font-size classes used in tag cloud.
	protected static float minSize = 1.0f;
	protected static float maxSize = 4.0f;

	// Display options
	protected boolean listView = false;
	protected boolean sortFreq = false;
	
	// Set true to hide the cloud view option
	protected boolean hideCloudView = false;

	// Display the tags for this user;
	protected User targetUser;

	public TagCloudPanel(String id, ITagLinkBuilder linkBuilder) {
		this(id, null, linkBuilder);
	}
	
	public TagCloudPanel(String id, Tag selectedTag, ITagLinkBuilder linkBuilder) {
		super(id);
		this.selected = selectedTag;
		this.tagLinkBuilder = linkBuilder;
		this.setOutputMarkupId(true);
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		container.add(AttributeModifier.replace("class", listView ? "tagList" : "tagCloud"));

		RefreshingView<TagPlusInt> tagView = new RefreshingView<TagPlusInt>("tagView") {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			protected Iterator getItemModels() {
				return new ModelIteratorAdapter(getTagsIterator()) {
					@Override
					protected IModel model(Object object) {
						return new CompoundPropertyModel(object);
					}
				};
			}

			@Override
			protected void populateItem(Item<TagPlusInt> item) {
				TagPlusInt entry = item.getModelObject();
				
				WebMarkupContainer link = getLink("link", entry);
				item.add(link);
				
				String className = getTagClass(entry);
				if (!Strings.isEmpty(className))
					link.add(new AttributeAppender("class", new Model<String>(className), " "));
				
				link.add(new TagLabel("tag", entry.getTag()));
				
				item.add(new Label("tagFreq", String.valueOf(entry.getInt())) {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() { return listView; }
				});
			}

		};
		container.add(tagView);
		
		add(new AjaxLink<Object>("setCloud") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				listView = false;
				target.add(TagCloudPanel.this);
			}
			@Override
			public boolean isVisible() {
				return listView && !hideCloudView;
			}
		});
		add(new AjaxLink<Object>("setList") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				listView = true;
				target.add(TagCloudPanel.this);
			}
			@Override
			public boolean isVisible() {
				return !listView;
			}
		});
		
		add(new AjaxLink<Object>("setAlpha") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				sortFreq = false;
				sortTagList();
				target.add(TagCloudPanel.this);
			}
			@Override
			public boolean isVisible() {
				return sortFreq;
			}
		});
		add(new AjaxLink<Object>("setFreq") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				sortFreq = true;
				sortTagList();
				target.add(TagCloudPanel.this);
			}
			@Override
			public boolean isVisible() {
				return !sortFreq;
			}
		});
	}
	
	protected String getTagClass (TagPlusInt tagPlusInt) {
		StringBuffer className = new StringBuffer("");
		if (!listView)
			className.append("s" + String.valueOf(getFontSize(tagPlusInt)));
		if (tagPlusInt.getTag().equals(selected))
			className.append(" selected");
		return className.toString().trim();
	}
	
	protected WebMarkupContainer getLink(String id, TagPlusInt entry) {
		if(additionalParameters != null) {
			additionalParameters.remove("tag");
			return tagLinkBuilder.buildLink(id, entry.getTag(), additionalParameters);
		}
		return tagLinkBuilder.buildLink(id, entry.getTag());
	}

	protected Iterator<TagPlusInt> getTagsIterator() {
		if (tagList == null)
			tagList = getTagList();
		return tagList.iterator();
	}
	
	public List<TagPlusInt> getTagList() {
		 tagList = TagService.get().countTagsForUser(getTargetUser());
		 sortTagList();
		 int maxFreq = 0;
		 for (TagPlusInt ti : tagList)
			 if (ti.getInt() > maxFreq)
				 maxFreq = ti.getInt();
		 this.maxFreq = maxFreq;
		 return tagList;
	}
	
	protected void sortTagList () {
		if (sortFreq) {
			Collections.sort(tagList, TagPlusIntFrequencyComparator.DESCENDING);
		} else {
			Collections.sort(tagList, TagPlusIntNameComparator.ASCENDING);			
		}
	}
	
	private int getFontSize (TagPlusInt ti) {
		if (maxFreq == 0)
			return Math.round(minSize);
		
		float weight = (maxSize - minSize) / maxFreq;
		float fontSize = minSize + ti.getInt() * weight;
		return Math.round(fontSize);
	}
	
	@Override
	public void onBeforeRender() {
		tagList = getTagList();
		super.onBeforeRender();
	}

	public PageParameters getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(PageParameters additionalParameters) {
		this.additionalParameters = additionalParameters;
	}
	
	public User getTargetUser() {
		if (targetUser ==  null)
			return CwmSession.get().getUser();
		return targetUser;
	}

	public void setTargetUser(User targetUser) {
		this.targetUser = targetUser;
	}
	
	public boolean hideCloudView() {
		return hideCloudView;
	}

	public void setHideCloudView(boolean hideCloudView) {
		if (hideCloudView)
			listView = true;
		this.hideCloudView = hideCloudView;
	}
}
