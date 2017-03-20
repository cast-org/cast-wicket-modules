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
import java.util.Map;

/**
 * Base class for various Figuration objects that can be shown and hidden.
 * 
 * @author bgoldowsky
 *
 * @param <T>
 */
public abstract class FigurationHideable<T> extends GenericPanel<T> {

	/**
	 * Whether the hide/show operation should be animated or not.
	 * If null, the framework's default will prevail.
	 */
	@Getter
	@Setter
	private Boolean animated = null;

	/**
	 * Duration of hide/show animation, in milliseconds.
	 * Only matters if {@link #animated} is true.  If null, the framework's default will be used.
	 */
	@Getter
	@Setter
	private Integer speed = null;

	public FigurationHideable(String id) {
		this(id, null);
	}

	public FigurationHideable(String id, IModel<T> model) {
		super(id, model);
		// setVersioned(false);
		// setVisible(false);
		setOutputMarkupPlaceholderTag(true);
		
		// set a default of empty content. 
		//add(new EmptyPanel(CONTENT_ID).setOutputMarkupPlaceholderTag(true));
	}

	/**
	 * Open a hideable object that may have multiple triggers.
	 * The triggering component must be specified, and it will be linked so that correct 
	 * ARIA attributes get set and focus can be restored to it when the hideable is closed.
	 * The 'unlink' parameter is passed so that the link will be broken after close.
	 * 
	 * @param trigger Component that will be marked as the triggering button.
	 * 	   This is important for ARIA annotation and appropriately setting focus on close.
	 * @param target Request target associated with current AJAX request.
	 */
	public void show(Component trigger, AjaxRequestTarget target) {
		Args.notNull(trigger, "Trigger component");
		Args.notNull(target, "AjaxRequestTarget");
		Args.isTrue(trigger.getOutputMarkupId(), "Hideable trigger component must output its markupId");
		// setVisible(true);
		// target.add(this);
		target.appendJavaScript( getShowMethodCall(trigger.getMarkupId()));
	}

	/**
	 * Open a hideable object that may have multiple triggers from a non-Wicket triggering element.
	 * In this version the HTML ID of the triggering element is specified rather than a Component.
	 * 
	 * ARIA attributes get set and focus can be restored to it when the hideable is closed.
	 * The 'unlink' parameter is passed so that the link will be broken after close.
	 * 
	 * @param triggerId ID of the trigger component in the HTML page.
	 * @param target Request target associated with current AJAX request.
	 */
	public void show(String triggerId, AjaxRequestTarget target) {
		Args.notNull(triggerId, "Trigger ID");
		Args.notNull(target, "AjaxRequestTarget");
		target.appendJavaScript(getShowMethodCall(triggerId));
	}
	
	/**
	 * Returns the Javascript method call to use for showing this object,
	 * including all configuration arguments needed.
	 * 
	 * @param triggerMarkupId  the id of the element triggering it to be shown.
	 * @return String of javascript.
	 */
	protected CharSequence getShowMethodCall(String triggerMarkupId) {
		return String.format("$('#%s').%s(%s);",
				triggerMarkupId, 
				getInitializationFunctionName(),
				formatAsJavascriptObject(getShowParameters()));
	}

	/**
	 * Returns a map of parameters that will be passed to the Figuration method when this
	 * object is to be shown.  These are passed as a JS Object to the initialization method.
	 * @return Map of parameter names to values.
	 */
	protected Map<String,String> getShowParameters() {
		Map<String,String> map = new HashMap<String, String>();
		map.put("toggle", "#" + this.getMarkupId());
		map.put("unlink", "true");
		if (animated != null)
			map.put("animate", animated.toString());
		if (speed != null)
			map.put("speed", speed.toString());
		return map;
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
	 * Hides this object.
	 * 
	 * @param target
	 *            Request target associated with current ajax request.
	 */
	public void hide(final AjaxRequestTarget target) {
		// setVisible(false);
		sendCommand(target, "hide");

	}

	/**
	 * Sends a Figuration-defined command to the modal window via javascript.
	 * Recognized commands are 'show', 'hide', 'toggle', 'unlink', and 'destroy'.
	 */
	protected void sendCommand(AjaxRequestTarget target, String command) {
		target.appendJavaScript(getCommandJavascript(command));
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
