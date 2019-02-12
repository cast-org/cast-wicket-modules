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
package org.cast.cwm.xml.transform;

import org.apache.wicket.util.time.Time;
import org.cast.cwm.xml.XmlSection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Classes implementing this interface can modify an {@link Element}.  IDOMTransformers
 * can be added through {@link XmlSection#addPreTransformer(IDOMTransformer)} and
 * {@link XmlSection#addPostTransformer(IDOMTransformer)}.  {@link #applyTransform(Node)}
 * will then be called at the appropriate time.
 * TODO update doc
 * 
 * @author jbrookover
 *
 */
public interface IDOMTransformer {
	
	/**
	 * Apply the transform to the given element.
	 * 
	 * @param n element
	 * @param params this can be null
	 * @return the transformed element
	 */
	public Element applyTransform(Element n, TransformParameters params);
	
	/**
	 * Return the last time this transformer was modified, or null if it is immutable.
	 * For a transformer based on an XSLT stylesheet, for instance, this would be the
	 * last modified time of that file.
	 * 
	 * @param params this can be null
	 * @return
	 */
	public Time getLastModified(TransformParameters params);

}
