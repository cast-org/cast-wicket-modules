/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.figuration.component;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.AppendingStringBuffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Base class for various Figuration objects that can be shown and hidden.
 * 
 * @author bgoldowsky
 *
 * @param <T>
 */
public abstract class FigurationHideable<T> extends GenericPanel<T> {

	/*
	 * Wicket IDs used for subparts of hideable widgets.
	 * Not all are used by all hideable components; eg a Popover only has a header and body.
	 */
	public static final String HEADER_ID = "header";
	public static final String BODY_ID = "body";
	public static final String FOOTER_ID = "footer";

	/**
	 * Possible ways to trigger hiding or showing a widget.
	 */
	public enum TriggerEvent { CLICK, HOVER, FOCUS, MANUAL }

	/**
	 * Should link to trigger element be removed when Hideable is hidden?
	 * If null, the framework's default will prevail.
	 */
	private Boolean unlink = null;

	/**
	 * How widget is triggered to open/close.
	 * If null, the framework's default will prevail.
	 */
	@Getter @Setter
	public Set<TriggerEvent> triggerEvents;

	/**
	 * ID of the button or other component that controls the opening/closing of the element.
	 * There has to be a trigger component in order for a Hideable to be initialized;
	 * it is used for ARIA markup and focus handling on close.
	 */
	@Getter @Setter
	public String triggerComponentId;

	/**
	 * Whether the hide/show operation should be animated or not.
	 * If null, the framework's default will prevail.
	 */
	@Getter @Setter
	private Boolean animated = null;

	/**
	 * Duration of hide/show animation, in milliseconds.
	 * Only matters if {@link #animated} is true.  If null, the framework's default will be used.
	 */
	@Getter @Setter
	private Integer speed = null;

	/**
	 * Keep this component within the bounds of an HTML element.
	 * The value of this field should be a jQuery selector to locate this element.
	 */
	@Getter @Setter
	private String viewport = null;

	public FigurationHideable(String id) {
		this(id, null);
	}

	public FigurationHideable(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupPlaceholderTag(true);
	}

	public void initialize(AjaxRequestTarget ajaxRequestTarget) {
		Args.notNull(ajaxRequestTarget, "AjaxRequestTarget");
		ajaxRequestTarget.appendJavaScript(getInitializeJavascript());
	}

	/**
	 * Send the command to show this component.
	 * @param ajaxRequestTarget AJAX channel
	 */
	public void show(AjaxRequestTarget ajaxRequestTarget) {
		Args.notNull(ajaxRequestTarget, "AjaxRequestTarget");
		sendCommand(ajaxRequestTarget, "show");
	}

	/**
	 * Hides this object.
	 *
	 * @param ajaxRequestTarget AJAX channel
	 */
	public void hide(AjaxRequestTarget ajaxRequestTarget) {
		sendCommand(ajaxRequestTarget, "hide");

	}
	/**
	 * Show a hideable object that may have multiple triggers.
	 * The triggering component must be specified, and it will be linked so that correct 
	 * ARIA attributes get set and focus can be restored to it when the hideable is closed.
	 * The 'unlink' parameter is passed so that the link will be broken after close.
	 *
	 * This method will initialize the Hideable if it isn't already initialized.
	 * 
	 * @param triggerComponent Component that will be marked as the triggering button.
	 * 	   This is important for ARIA annotation and appropriately setting focus on close.
	 * @param ajaxRequestTarget Request target associated with current AJAX request.
	 */
	public void connectAndShow(Component triggerComponent, AjaxRequestTarget ajaxRequestTarget) {
		Args.notNull(triggerComponent, "Trigger component");
		Args.notNull(ajaxRequestTarget, "AjaxRequestTarget");
		Args.isTrue(triggerComponent.getOutputMarkupId(), "Hideable trigger component must output its markupId");
		triggerComponentId = triggerComponent.getMarkupId();
		unlink = true;
		initialize(ajaxRequestTarget);
		show(ajaxRequestTarget);
	}

	/**
	 * Open a hideable object that may have multiple triggers from a non-Wicket triggering element.
	 * In this version the HTML ID of the triggering element is specified rather than a Component.
	 * The triggering element will be linked so that correct
	 * ARIA attributes get set and focus can be restored to it when the hideable is closed.
	 * The 'unlink' parameter is passed so that the link will be broken after close.
	 *
	 * This method will initialize the Hideable if it isn't already initialized.
	 *
	 * @param triggerComponentId ID of the trigger component in the HTML page.
	 * @param ajaxRequestTarget Request target associated with current AJAX request.
	 */
	public void connectAndShow(String triggerComponentId, AjaxRequestTarget ajaxRequestTarget) {
		Args.notNull(triggerComponentId, "Trigger ID");
		Args.notNull(ajaxRequestTarget, "AjaxRequestTarget");
		this.triggerComponentId = triggerComponentId;
		unlink = true;
		initialize(ajaxRequestTarget);
		show(ajaxRequestTarget);
	}

	/**
	 * Returns the Javascript method call to use for showing this object,
	 * including all configuration arguments needed.
	 * 
	 * @return String of javascript.
	 */
	protected CharSequence getInitializeJavascript() {
		if (triggerComponentId == null)
			throw new IllegalStateException("Cannot initialize - no trigger component ID is set.");
		return String.format("$('#%s').%s(%s);",
				triggerComponentId,
				getInitializationFunctionName(),
				formatAsJavascriptObject(getInitializeParameters()));
	}

	/**
	 * Returns a map of parameters that will be passed to the Figuration method when this
	 * object is to be shown.  These are passed as a JS Object to the initialization method.
	 * @return Map of parameter names to values.
	 */
	protected Map<String,String> getInitializeParameters() {
		Map<String,String> map = new HashMap<String, String>();
		map.put("target", "#" + this.getMarkupId());
		if (triggerEvents!=null && !triggerEvents.isEmpty())
			map.put("trigger", formatTriggerEvents(triggerEvents));
		if (unlink != null)
			map.put("unlink", unlink.toString());
		if (animated != null)
			map.put("animate", animated.toString());
		if (speed != null)
			map.put("speed", speed.toString());
		if (viewport != null)
			map.put("viewport", viewport);
		return map;
	}

	protected String formatTriggerEvents(Set<TriggerEvent> triggerEvents) {
		StringBuilder builder = new StringBuilder();
		Iterator<TriggerEvent> it= triggerEvents.iterator();
		while (it.hasNext()) {
			TriggerEvent e = it.next();
			builder.append(e.name().toLowerCase());
			if (it.hasNext())
				builder.append(" ");
		}
		return builder.toString();
	}

	/**
	 * Lightweight JSON conversion for simple String->String maps.
	 * @return a literal Javascript object as a string
	 */
	protected String formatAsJavascriptObject(Map<String,String> map) {
		AppendingStringBuffer buf = new AppendingStringBuffer();
		buf.append("{");
		for (Map.Entry<String,String> entry : map.entrySet()) {
			String key = entry.getKey();
			if (entry.getValue().equals("false"))
				// Convert string value to an actual boolean, since "false" evaluates to true in JS.
				buf.append(key).append(": false, ");
			else
				buf.append(key).append(": '").append(entry.getValue()).append("', ");
		}
		buf.append("}");
		return buf.toString();
	}

	/**
	 * Sends a Figuration-defined command to the modal window via javascript.
	 * Recognized commands are 'show', 'hide', 'toggle', 'unlink', and 'destroy'.
	 */
	protected void sendCommand(AjaxRequestTarget ajaxRequestTarget, String command) {
		ajaxRequestTarget.appendJavaScript(getCommandJavascript(command));
	}

	/**
	 * Defines the javascript that should be run to send a command to this Figuration object.
	 * Command string should be one of the defined commands such as 'toggle', 'show', 'hide', etc.
	 *
	 * @param command command string, see Figuration documentation for appropriate widget
	 * @return a javascript statement
	 */
	public String getCommandJavascript(String command) {
		return String.format("$('#%s').%s('%s');",
				getMarkupId(),
				getInitializationFunctionName(),
				command);
	}

	/**
	 * Should return the Javascript method that is called to initialize this type of object.
	 * @return name of the javascript initialization function as defined by Figuration
	 */
	public abstract String getInitializationFunctionName();

}
