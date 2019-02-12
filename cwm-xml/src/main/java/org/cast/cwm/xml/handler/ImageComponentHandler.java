/*
 * Copyright 2011-2019 CAST, Inc.
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
package org.cast.cwm.xml.handler;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ResourceReference;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.xml.IXmlPointer;
import org.w3c.dom.Element;

public class ImageComponentHandler extends BaseDynamicComponentHandler implements IDynamicComponentHandler {

	public ImageComponentHandler(String prefix) {
		super(prefix);
	}

	public Component getComponent(String wicketId, Element element,
			IRelativeLinkSource linkSource, IModel<? extends IXmlPointer> secMod) {
		String src = element.getAttributeNS(null, "src");
		ResourceReference imgRef = linkSource.getRelativeReference(src);
		return new Image(wicketId, imgRef);
	}

}
