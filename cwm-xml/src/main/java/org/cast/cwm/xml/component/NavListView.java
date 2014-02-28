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
package org.cast.cwm.xml.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;

/**
 * Base class for creating navigation lists.
 * An item will be added for each child of the given XmlSection.
 * Normally, this item is a link containing a label.
 * The method to generate the link is abstract and must be specified.
 * A "current" XmlSection can be specified; if so then a class attribute
 * is by default added to that section or its ancestor if found in the list.
 */
public abstract class NavListView extends ChildListView {
	
	private IModel<XmlSection> currentSectionModel = null;

	private static final long serialVersionUID = 1L;

	public NavListView(String id, XmlSection sec) {
		this(id, new XmlSectionModel(sec));
	}
	
	public NavListView(String id, IModel<XmlSection> model) {
		super(id, model);
	}
	
	public NavListView(String id, IModel<XmlSection> model, IModel<XmlSection> currentSectionModel) {
		this(id, model);
		this.currentSectionModel = currentSectionModel;
	}
	
	public NavListView(String id, XmlSection section, XmlSection currentSection) {
		this(id, new XmlSectionModel(section), new XmlSectionModel(currentSection));
	}
	
	@Override
	protected void populateItem(ListItem<XmlSection> item) {
		XmlSection sec = item.getModelObject();
		item.add( makeLink(sec).add(makeLabel(sec)));
		String classAttribute = makeAttributeString(item);
		if (classAttribute != null)
			item.add(AttributeModifier.replace("class", classAttribute));
	}
	
	/** Return a string to use for the class attribute of the list item.
	 * This implementation handles a "first" attribute for the first item in the list
	 * and a "current" attribute for the current item in the navigation menu,
	 * if any.  Can be overridden as necessary.
	 * @param ListItem
	 * @return string to use as class attribute.  If null, class attribute from markup file will not be changed.
	 */
	protected String makeAttributeString (ListItem<XmlSection> item) {
		StringBuffer attString = new StringBuffer("");
		
		// Add an attribute if item is first in the list
		if (item.getIndex() == 0)
			attString.append("first");

		if (isCurrent(item.getModelObject())) {
			if (attString.length() > 0)
				attString.append(" ");
			attString.append("current");			
		}
		if (attString.length() > 0)
			return attString.toString();
		else 
			return null;
	}

	/** Must be overridden to return a link to the given XmlSection */
	protected abstract WebMarkupContainer makeLink (XmlSection sec);
	
	protected Component makeLabel (XmlSection sec) {
		return new Label("title", sec.getTitle());
	}

	// Check if item is current (or includes current as a descendant)
	public boolean isCurrent(XmlSection sec) {
		if (currentSectionModel == null)
			return false;
		XmlSection currentSection = currentSectionModel.getObject();
		return (sec.equals(currentSection) || sec.isAncestorOf(currentSection));
	}
	
	public IModel<XmlSection> getCurrentSectionModel() {
		return currentSectionModel;
	}

	public void setCurrentSectionModel(IModel<XmlSection> currentSectionModel) {
		this.currentSectionModel = currentSectionModel;
	}

}
