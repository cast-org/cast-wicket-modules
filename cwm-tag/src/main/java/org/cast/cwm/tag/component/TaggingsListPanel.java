/*
 * Copyright 2011-2013 CAST, Inc.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.cast.cwm.tag.ITagLinkBuilder;
import org.cast.cwm.tag.TagService;
import org.cast.cwm.tag.model.Tagging;

/**
 * A list of tags, with links to remove, for a given object.
 * 
 * @author jbrookover
 *
 */
public class TaggingsListPanel extends Panel {

	List<Tagging> tings = new ArrayList<Tagging>();
	protected final ITagLinkBuilder tagLinkBuilder;
	protected boolean showRemoveLinks = true;
	protected int maxResults = Integer.MAX_VALUE;
	protected String noTagMessage = "";
	private int count = 0;
	private User user;

	private static final long serialVersionUID = 1L;

	public TaggingsListPanel(final String id, final PersistedObject target, ITagLinkBuilder linkBuilder) {
		this(id, target, linkBuilder, null);
		
	}
	
	public TaggingsListPanel(final String id, final PersistedObject target, ITagLinkBuilder linkBuilder, final User student) {
		super(id);
		this.tagLinkBuilder = linkBuilder;
		this.user = (student == null ? CwmSession.get().getUser() : student);
		setOutputMarkupId(true);
		
		add(new RefreshingView<Tagging>("tagging") {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			protected Iterator getItemModels() {
				tings = TagService.get().taggingsForTarget(user, target);

				return new ModelIteratorAdapter(tings.iterator()) {
					@Override
					protected IModel model(Object object) {
						return new CompoundPropertyModel(
								object);
					}
				};
			}

			@Override
			protected void populateItem(Item<Tagging> item) {
				final Tagging ting = item.getModelObject();
				
				WebMarkupContainer link = tagLinkBuilder.buildLink("link", ting.getTag());
				item.add(link);
				link.add(new TagLabel("name", ting.getTag()));
				
				item.add(new AjaxLink<Void>("remove") {
					private static final long serialVersionUID = 1L;
					
					@Override
					public boolean isVisible() {
						return showRemoveLinks && student == null;
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						TagService.get().removeTagging(ting);
						target.add(TaggingsListPanel.this);
						target.addChildren(findParent(TagPanel.class), TagList.class);
					}
				});
				count++;
				if (count > maxResults)
					item.setVisible(false);
			
			}
		});
		add(new Label("moreTags", "...") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean isVisible() {
				return count > maxResults;
			}
			
		});
		
		add(new Label("noTags", new AbstractReadOnlyModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return noTagMessage;
			}
			
		}) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return count == 0 && noTagMessage != null && !noTagMessage.matches("");
			}
		});
	}
	
	@Override
	public void onBeforeRender() {
		count = 0;
		super.onBeforeRender();
	}

	public boolean isShowRemoveLinks() {
		return showRemoveLinks;
	}

	public void setShowRemoveLinks(boolean showRemoveLinks) {
		this.showRemoveLinks = showRemoveLinks;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}	
	
	public List<Tagging> getCurrentTaggings() {
		return this.tings;
	}

	public String getNoTagMessage() {
		return noTagMessage;
	}

	public void setNoTagMessage(String noTagMessage) {
		this.noTagMessage = noTagMessage;
	}

}
