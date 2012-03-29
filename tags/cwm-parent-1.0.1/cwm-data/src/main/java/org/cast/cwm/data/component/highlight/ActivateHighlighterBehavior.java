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
package org.cast.cwm.data.component.highlight;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.service.HighlightService;
import org.cast.cwm.service.HighlightService.HighlightType;

/**
 * Adding this behavior to a component allows it to interact with the
 * client-side highlighting javascript.  It will Activate the provided
 * highlighter (onclick) and receive class modifications based on whether
 * this highlighter is active and has highlights.
 * 
 * @author jbrookover
 *
 */
public class ActivateHighlighterBehavior extends AbstractBehavior {
	
	private static final long serialVersionUID = 1L;
	private HighlightType type;
	
	public ActivateHighlighterBehavior(Character c) {
		this(HighlightService.get().getHighlighter(c));
	}

	public ActivateHighlighterBehavior(HighlightType type) {
		if (type == null)
			throw new IllegalArgumentException("HighlightType cannot be null; Have you registered this highlighter?");
		this.type = type;
	}
	
	@Override
	public void bind(Component component) {
		component.add(new ClassAttributeModifier("control" + type.getColor()));
		component.add(new SimpleAttributeModifier("onclick", "javascript:changeMode('" + type.getColor() + "');"));
	}
}
