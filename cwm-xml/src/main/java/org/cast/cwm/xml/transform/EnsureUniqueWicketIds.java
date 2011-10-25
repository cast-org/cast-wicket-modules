/*
 * Copyright 2011 CAST, Inc.
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
package org.cast.cwm.xml.transform;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.time.Time;
import org.cast.cwm.xml.service.XmlService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class EnsureUniqueWicketIds implements IDOMTransformer {

	private static final String WICKET_NS = "http://wicket.apache.org";

	public Element applyTransform(Element elt, TransformParameters params) {
		List<String> wicketIds = new ArrayList<String>();
		NodeList wicketChildren = XmlService.get().getWicketNodes(elt, false);
		
		for (int i=0; i < wicketChildren.getLength(); i++) {
			Element child = (Element) wicketChildren.item(i);
			String id = child.getAttributeNS(WICKET_NS, "id");
			if (!wicketIds.contains(id)) {
				wicketIds.add(id);
			} else {
				int count = 1;
				String newId = id + count;
				while(wicketIds.contains(newId))
					newId = id + count++;
				wicketIds.add(newId);
				child.setAttributeNS(WICKET_NS, "id", newId);
				// log.trace("Found Duplicate Wicket Id ({}) when processing DOM Tree.  Generated new Id ({})", id, newId);
			}
			applyTransform(child, params);
		}
		return elt;
	}

	public Time getLastModified(TransformParameters params) {
		return null;  // this transformation will not change over time.
	}

}
