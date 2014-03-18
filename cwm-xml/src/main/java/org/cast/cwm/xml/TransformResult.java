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
package org.cast.cwm.xml;

import java.io.Serializable;

import lombok.Getter;

import org.cast.cwm.xml.service.XmlService;
import org.w3c.dom.Element;

/**
 * Holds both the Element and serialized String form of the result of a transformation.
 * The serialized string version will be created as necessary and then cached.
 * 
 * @author borisgoldowsky
 */
public class TransformResult implements Serializable {

	@Getter
	protected Element element;
	
	protected String string;
	
	private static final long serialVersionUID = 1L;

	public TransformResult (Element element) {
		this.element = element;
		this.string = null;
	}
	
	public String getString() {
		if (string == null && element != null) {
			string = XmlService.get().serialize(element);
		}
		return string;
	}
}
