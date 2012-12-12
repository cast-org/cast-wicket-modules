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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.cast.cwm.tag.ITagLinkBuilder;

/**
 * Standard interface for adding tags to a target.  Displays the target's current tags,
 * a list of available/previous tags, and a form to add a new tag.
 * 
 * @author jbrookover
 *
 */
public class TagPanel extends Panel {

	private static final long serialVersionUID = 1L;

	final PersistedObject target;
	
	protected TaggingsListPanel currentListing;
	protected TagList tagListing;
	protected User student;
	
	
	/**
	 * Create a interactive tag panel for the current user.
	 * 
	 * @param id
	 * @param target
	 * @param linkBuilder
	 */
	public TagPanel(final String id, final PersistedObject target, ITagLinkBuilder linkBuilder) {
		this(id, target, linkBuilder, null);
	}
	
	/**
	 * Create a read-only tag panel for the specified student.
	 * 
	 * @param id
	 * @param target
	 * @param linkBuilder
	 * @param s - The student whose tags should be displayed.
	 */
	public TagPanel(final String id, final PersistedObject target, ITagLinkBuilder linkBuilder, User s) {
		super(id);
		this.setOutputMarkupId(true);
		this.target = target;
		this.student = s;
		add(currentListing = new TaggingsListPanel("current", target, linkBuilder, student));
		add(tagListing = new TagList("taglist", target, student));
		if (student == null)
			add(new AddTagForm("form", target));
		else
			add(new WebMarkupContainer("form").setVisible(false));
		if (student == null)
			add(new Label("tagsSubHeader", "Add Tags:"));
		else
			add(new Label("tagsSubHeader", "Available Tags:"));
		
	}

}
