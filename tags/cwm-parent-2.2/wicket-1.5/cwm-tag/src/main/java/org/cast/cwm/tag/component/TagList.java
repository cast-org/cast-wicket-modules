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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.cast.cwm.tag.TagService;
import org.cast.cwm.tag.model.Tag;
import org.cast.cwm.tag.model.Tagging;

/**
 * A list of available tags that a user can add to an object (?).
 * 
 * @author jbrookover
 *
 */
public class TagList extends WebMarkupContainer {
	
	final PersistedObject target;
	List<Tag> allUserTags = null;
	final User user;

	private static final long serialVersionUID = 1L;

	public TagList (String id, final PersistedObject target) {
		this(id, target, null);
	}
	
	public TagList (String id, final PersistedObject target, final User student) {
		super(id);
		this.setOutputMarkupId(true);
		this.target = target;
		this.user = student == null ? CwmSession.get().getUser() : student;
		
		RefreshingView<Tag> list = new RefreshingView<Tag>("tag") {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			protected Iterator getItemModels() {
				allUserTags = TagService.get().tagsForUser(user);

				List<Tag> tags = new ArrayList<Tag>(allUserTags);
				for (Tagging ting : TagService.get().taggingsForTarget(user, target)) {
					tags.remove(ting.getTag());
				}
				
				return new ModelIteratorAdapter<Tag>(tags.iterator()) {
					
					@Override
					protected IModel<Tag> model(Tag object) {
						return new CompoundPropertyModel(object);
					}
				};
			}

			@Override
			protected void populateItem(Item<Tag> item) {
				final Tag t = item.getModelObject();
				AjaxLink<Void> link = new AjaxLink<Void>("link") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick(AjaxRequestTarget ajaxtarget) {
						TagService.get().findTaggingCreate(user, target, t.getName());
						ajaxtarget.addComponent(TagList.this);
						ajaxtarget.addChildren(findParent(TagPanel.class), TaggingsListPanel.class);
					}
				};
				link.setEnabled(student == null);
				item.add(link);
				link.add(new TagLabel("name", t));				
			}
		};
		add(list);
	}

}
