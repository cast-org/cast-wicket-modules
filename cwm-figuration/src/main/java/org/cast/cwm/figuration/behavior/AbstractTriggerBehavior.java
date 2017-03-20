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
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.lang.Args;
import org.cast.cwm.figuration.TriggerType;

import java.util.List;

/**
 * Base class for triggering behaviors for modals, popovers, etc.
 * 
 * @author bgoldowsky
 *
 */
public abstract class AbstractTriggerBehavior extends Behavior {

	protected final String widgetType;

	@Getter
	@Setter
	private String toggleId;
	
	/**
	 * What types of events (click, hover, etc) trigger the popover's display.
	 */
	@Getter
	@Setter
	protected List<TriggerType> triggers = null;
	
	/**
	 * If set, popover behavior will be initialized by CFW based on the attributes set.
	 * Set this to false if you want to initialize later via Javascript (eg, to set a 
	 * placement function).
	 */
	@Getter
	@Setter
	protected boolean initializeOnLoad = true;
	

	public AbstractTriggerBehavior(String widgetType, String toggleId) {
		Args.notEmpty(toggleId, "toggle ID");
		this.widgetType = widgetType;
		this.toggleId = toggleId;
	}
	
	@Override
	public void onConfigure(Component component) {
		super.onConfigure(component);
		// Trigger components must have a markupId available to Javascript.
		component.setOutputMarkupId(true);
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		super.onComponentTag(component, tag);
		
		if (isInitializeOnLoad())
			tag.put("data-cfw", widgetType);

		tag.put("data-cfw-" + widgetType + "-toggle", "#"+getToggleId());
		
		String triggerAtt = getTriggerAttribute();
		if (triggerAtt != null)
			tag.put("data-cfw-" + widgetType + "-trigger", triggerAtt);
	}
	
	/**
	 * Return the string to use as the trigger value (types of action that will open the popover).
	 * This is normally the values of {@link #triggers}, joined with spaces.
	 * 
	 * @return string value or null (meaning no attribute; CFW's defautl will be used)
	 */
	protected String getTriggerAttribute() {
		List<TriggerType> triggerValues = getTriggers();
		if (triggerValues != null && !triggerValues.isEmpty()) {
			StringBuilder value = new StringBuilder();
			for (TriggerType t : triggerValues)
				value.append(t.name().toLowerCase()).append(' ');
			value.deleteCharAt(value.length()-1); // remove trailing space
			return value.toString();
		} else {
			return null;
		}
	}

}