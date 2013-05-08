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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.Model;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.tag.TagService;

/**
 * A form that allows you to create a new tag for a given object.
 * 
 * @author jbrookover
 *
 */
public class AddTagForm extends Form<Object> {
	
	protected Class<? extends PersistedObject> targetType;
	protected Long targetId;
	
	private static final long serialVersionUID = 1L;
	private RequiredTextField<String> input;

	public AddTagForm(String id, PersistedObject target) {
		super(id);
		setOutputMarkupId(true);
		targetType = target.getClass();
		targetId = target.getId();
		
		input = new RequiredTextField<String>("term", new Model<String>(""));
		input.setOutputMarkupId(true);
		input.add(new SimpleAttributeModifier("maxlength", Integer.toString(TagService.get().getMaxTagLength())));
		add(input);
		
		add(new AjaxButton("add", this) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				TagService.get().findTaggingCreate(CwmSession.get().getUser(), targetType, targetId, input.getModelObject());
				input.setModelObject(""); // clear input box
				target.addComponent(input);
				MarkupContainer tp = findParent(TagPanel.class);
				if (tp != null) {
					target.addChildren(tp, TaggingsListPanel.class);
					target.addChildren(tp, TagList.class);
				}
			}
		});
	}
	
}
