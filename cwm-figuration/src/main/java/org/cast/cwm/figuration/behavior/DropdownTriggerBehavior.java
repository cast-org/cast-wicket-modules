/*
 * Copyright 2011-2016 CAST, Inc.
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
package org.cast.cwm.figuration.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.cast.cwm.figuration.TriggerType;

/**
 * When attached to a component, adds the Figuration attributes that make it open a dropdown menu.
 * Various setter methods are available to tune the behavior - whether to open on 
 * click or hover, etc.
 *
 */
public class DropdownTriggerBehavior extends AbstractTriggerBehavior {

	/**
	 * Construct with a given markupId for the component to be toggled.
	 * Caller is responsible for making sure that that element exists, and is a Figuration Modal.
	 * @param toggleId ID of the modal
	 */
	public DropdownTriggerBehavior(String toggleId) {
		super("dropdown", toggleId);
	}

	// Dropdown doesn't use the same trigger attribute as popover and modal,
	// since its options are more restricted.
	@Override
	protected String getTriggerAttribute() {
		return null;
	}
	
	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		super.onComponentTag(component, tag);
		if (getTriggers().contains(TriggerType.HOVER))
			tag.put("data-cfw-dropdown-hover", "true");
	}
	
}