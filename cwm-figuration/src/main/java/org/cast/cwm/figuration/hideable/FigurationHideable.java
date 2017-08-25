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
package org.cast.cwm.figuration.hideable;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
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
	 * How widget is triggered to open/close.
	 * If null, the framework's default will prevail.
	 */
	@Getter @Setter
	public Set<TriggerType> triggerEvents;

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
	 * Where in the DOM the hideable element will be attached.
	 * In some circumstances they must be moved from where they are defined in the HTML,
	 * and attached, for instance, to the HTML body element so that they will display
	 * properly.
	 */
	@Getter @Setter
	private String container = null;

	/**
	 * Keep this hideable within the bounds of an HTML element.
	 * The value of this field should be a jQuery selector to locate this element.
	 */
	@Getter @Setter
	private String viewport = null;

	/**
	 * In order to be shown, a Hideable must be linked linked to a single trigger component.
	 * The trigger controls positioning, they are linked via ARIA markup, and it provides
	 * a place for focus to be restored when a the hideable is removed.
	 * The trigger component can be null and can be changed while a Hideable is in the hidden state.
	 */
	@Getter @Setter
	private Component triggerComponent = null;

	/**
	 * Should link to trigger element be removed when Hideable is hidden?
	 * If null, the framework's default will prevail.
	 */
	protected Boolean unlink = null;

	public FigurationHideable(String id) {
		this(id, null);
	}

	public FigurationHideable(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupPlaceholderTag(true);
		addClassAttributeModifier();
	}

	/**
	 * Sets up a ClassAttributeModifier that adds the figuration-required class attribute to the top level tag.
	 */
	protected void addClassAttributeModifier() {
		add(ClassAttributeModifier.append("class", getClassAttribute()));
	}

	/**
	 * Send the javascript command to show this hideable.
	 *
	 * @param ajaxRequestTarget required ajax context
	 */
	public void show(AjaxRequestTarget ajaxRequestTarget) {
		appendCommand(ajaxRequestTarget, "show");
	}

	/**
	 * Send the javascript command to show this hideable before other ajax operations.
	 * (See {@link #prependCommand(AjaxRequestTarget, String)})
	 *
	 * @param ajaxRequestTarget required ajax context
	 */
	public void prependShow(AjaxRequestTarget ajaxRequestTarget) {
		prependCommand(ajaxRequestTarget, "show");
	}

	/**
	 * Send the javascript command to hide this object.
	 *
	 * @param ajaxRequestTarget required ajax context
	 */
	public void hide(AjaxRequestTarget ajaxRequestTarget) {
		appendCommand(ajaxRequestTarget, "hide");
	}

	/**
	 * Send the javascript command to hide this object before other ajax operations.
	 * (See {@link #prependCommand(AjaxRequestTarget, String)})
	 *
	 * @param ajaxRequestTarget required ajax context
	 */
	public void prependHide(AjaxRequestTarget ajaxRequestTarget) {
		prependCommand(ajaxRequestTarget, "hide");
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
		this.triggerComponent = triggerComponent;
		unlink = true;
		initialize(ajaxRequestTarget);
		show(ajaxRequestTarget);
	}

	public void initialize(AjaxRequestTarget ajaxRequestTarget) {
		ajaxRequestTarget.appendJavaScript(getInitializeJavascript());
	}

	/**
	 * Returns the Javascript method call to use for showing this object,
	 * including all configuration arguments needed.
	 *
	 * @return String of javascript.
	 */
	protected CharSequence getInitializeJavascript() {
		if (triggerComponent == null)
			throw new IllegalStateException("Can't initialize; no trigger component is set");
		return String.format("$('#%s').%s(%s);",
				triggerComponent.getMarkupId(),
				getInitializationFunctionName(),
				formatAsJavascriptObject(getInitializeParameters()));
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
	 * Returns a map of parameters that are used to initialize the Figuration widget.
	 * These are passed as a JS Object to the initialization method.
	 * @return Map of parameter names to values.
	 */
	protected Map<String,String> getInitializeParameters() {
		Map<String,String> map = new HashMap<>();
		map.put("target", "#" + this.getMarkupId());
		if (triggerEvents!=null && !triggerEvents.isEmpty())
			map.put("trigger", formatTriggerEvents(triggerEvents));
		if (unlink != null)
			map.put("unlink", unlink.toString());
		if (animated != null)
			map.put("animate", animated.toString());
		if (speed != null)
			map.put("speed", speed.toString());
		if (container != null)
			map.put("container", container);
		if (viewport != null)
			map.put("viewport", viewport);
		return map;
	}

	protected String formatTriggerEvents(Set<TriggerType> triggerEvents) {
		StringBuilder builder = new StringBuilder();
		Iterator<TriggerType> it= triggerEvents.iterator();
		while (it.hasNext()) {
			TriggerType e = it.next();
			builder.append(e.name().toLowerCase());
			if (it.hasNext())
				builder.append(" ");
		}
		return builder.toString();
	}

	/**
	 * Sends a Figuration-defined command to the modal window via javascript.
	 * Recognized commands are 'show', 'hide', 'toggle', 'unlink', and 'destroy'.
	 */
	protected void appendCommand(AjaxRequestTarget ajaxRequestTarget, String command) {
		ajaxRequestTarget.appendJavaScript(getCommandJavascript(command));
	}

	/**
	 * Sends a Figuration-defined command to the modal window via javascript, to be done BEFORE other ajax operations.
	 * Normally hide & show commands are appended to the end of the AJAX operation, but
	 * sometimes prepending is necessary, for example when closing a window BEFORE removing its triggering button.
	 * Recognized commands are 'show', 'hide', 'toggle', 'unlink', and 'destroy'.
	 */
	protected void prependCommand(AjaxRequestTarget ajaxRequestTarget, String command) {
		ajaxRequestTarget.prependJavaScript(getCommandJavascript(command));
	}

	/**
	 * Defines the javascript that should be run to send a command to this Figuration object.
	 * Command string should be one of the defined commands such as 'toggle', 'show', 'hide', etc.
	 *
	 * @param command command string, see Figuration documentation for appropriate widget
	 * @return a javascript statement
	 */
	protected String getCommandJavascript(String command) {
		return String.format("$('#%s').%s('%s');",
				getMarkupId(),
				getInitializationFunctionName(),
				command);
	}

	/**
	 * Should return the Javascript method that is called to initialize this type of object.
	 * @return name of the javascript initialization function as defined by Figuration
	 */
	protected abstract String getInitializationFunctionName();

	/**
	 * Should return the Figuration-defined class name that distinguishes this type of hideable.
	 * @return a class attribute to add to this component's tag
	 */
	protected abstract String getClassAttribute();

}
