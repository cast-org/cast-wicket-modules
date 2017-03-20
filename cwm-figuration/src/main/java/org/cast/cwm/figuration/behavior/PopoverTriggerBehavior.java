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

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.figuration.PopoverPlacement;
import org.cast.cwm.figuration.component.FigurationPopover;

/**
 * When attached to a component, adds the CFW attributes that make it open a popover.
 * Various setter methods are available to tune the behavior - whether to open on 
 * click or hover, the placement of the popover, etc.
 * Can also add an additional 'data-event-info' attribute for help in event logging.
 * 
 * TODO: CFW popovers have a lot of additional configuration options that are not yet exercised here.
 * Currently, only linking to a separate popover component is handled, but the entire popover
 * content could be specified through attributes on the trigger element.
 * 
 * @author bgoldowsky
 *
 */
public class PopoverTriggerBehavior extends AbstractTriggerBehavior {

	/**
	 * Whether popover should be placed above, below, left or right of this trigger.
	 */
	@Getter
	@Setter
	private PopoverPlacement placement = null;
	
	/**
	 * If true, popover will be flipped to the other side of the triggerring element
	 * if there is not room for it in the specified placement.
	 */
	@Getter
	@Setter
	private boolean autoSwitchPlacement = true;
	
	/**
	 * Where the popover element will be attached; usually "body".
	 */
	@Getter
	@Setter
	private String popoverContainer = null;
	
	/**
	 * Construct with a given FigurationPopover as the component to be toggled.
	 * @param toggle
	 */
	public PopoverTriggerBehavior(FigurationPopover<?> toggle) {
		this(toggle.getMarkupId());
		Args.isTrue(toggle.getOutputMarkupId(), "Target must output its markup ID");
	}
	
	/**
	 * Construct with a given markupId for the component to be toggled.
	 * Caller is responsible for making sure that that element exists, and is a popover.
	 * @param toggleId
	 */
	public PopoverTriggerBehavior(String toggleId) {
		super("popover", toggleId);
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		super.onComponentTag(component, tag);
		
		String place = getPlacementAttribute();
		if (place != null)
			tag.put("data-cfw-popover-placement", place);
		
		String containerValue = getPopoverContainer();
		if (!Strings.isEmpty(containerValue))
			tag.put("data-cfw-popover-container", containerValue);
	}

	protected String getPlacementAttribute() {
		PopoverPlacement place = getPlacement();
		String autoExtension = isAutoSwitchPlacement() ? " auto" : "";
		if (place != null) {
			return place.name().toLowerCase() + autoExtension;
		} else {
			return null;
		}
	}
	

}
