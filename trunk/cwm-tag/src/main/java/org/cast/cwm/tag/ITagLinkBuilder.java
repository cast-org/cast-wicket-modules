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
package org.cast.cwm.tag;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.tag.model.Tag;

public interface ITagLinkBuilder extends Serializable {
	
	/**
	 * Construct an appropriate link (or other component) for the given tag.
	 * This will be called to make components to wrap tag names in 
	 * a cloud or list of tags.
	 * 
	 * @param id  wicket:id to give the component
	 * @param tag
	 * @return a link, or any instance of WebMarkupContainer
	 */
	public WebMarkupContainer buildLink (String id, Tag tag);
	
	public WebMarkupContainer buildLink (String id, Tag tag, PageParameters parameters);

}
