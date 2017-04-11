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
package org.cast.cwm.data.behavior;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * This behavior will submit a form at regular intervals.  Any HTML element
 * with class='autosave_info' will display the status of these automatic
 * saves.
 * 
 * @author jbrookover
 *
 */
public class AjaxAutoSavingBehavior extends AjaxFormSubmitBehavior {

	private static final long serialVersionUID = 1L;
	
	private static long updateInterval = 30000; 

	private static final String AUTOSAVE_EVENT = "autosave";
	
	public static final PackageResourceReference AUTOSAVING_JAVASCRIPT = new PackageResourceReference(AjaxAutoSavingBehavior.class, "AjaxAutoSavingBehavior.js");

	/**
	 * Constructor - attach to a component INSIDE the form.
	 */
	public AjaxAutoSavingBehavior() {
		super(AUTOSAVE_EVENT);
		init();
	}
	
	/**
	 * Constructor - attach to a component outside the form
	 * or the form itself.
	 * 
	 * @param form
	 */
	public AjaxAutoSavingBehavior(Form<?> form) {
		super(form, AUTOSAVE_EVENT);
		init();
	}
	
	/**
	 * Initialize the Autosave behavior for this form.
	 */
	protected void init() {
		getForm().setOutputMarkupId(true);
		getForm().add(AttributeModifier.append("class", "ajaxAutoSave"));
	}

	@Override
	protected void onError(AjaxRequestTarget target) {
		
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		// This behavior is not triggered by an event; skip super.onComponentTag() to prevent writing javascript.
	}

	@Override
	protected final void onSubmit(AjaxRequestTarget target) {
		if (RequestCycle.get().getRequest().getRequestParameters().getParameterValue("autosave").toBoolean()) {	
			onAutoSave(target);
		} else {
			throw new IllegalStateException("Autosaving request expected parameter autosave='true'");
		}
	}
	
	@Override
	public void renderHead(Component component, final IHeaderResponse response) {
		super.renderHead(component, response);
		
		// Run once to initialize
		response.render(JavaScriptHeaderItem.forReference(AUTOSAVING_JAVASCRIPT));
		response.render(JavaScriptHeaderItem.forScript("AutoSaver.setup(" + updateInterval + ");", "AjaxAutoSavingBehaviorSetup"));
		
		response.render(OnDomReadyHeaderItem.forScript("AutoSaver.makeLinksSafe();"));

		// Run each time to register this Form's default values and call-back URL with the AutoSaver
		response.render(OnDomReadyHeaderItem.forScript("AutoSaver.addForm('" + getForm().getMarkupId() + "', '" + this.getCallbackUrl() + "');"));
	}

	/**
	 * Called when the form auto-submits itself.  By default, this does nothing and presumes
	 * the form should process silently in the background.  Override this method to provide
	 * additional ajax changes to the page.
	 * 
	 * @param target
	 */
	protected void onAutoSave(AjaxRequestTarget target) {
		
	}
	
	/**
	 * Set the AutoSaving update interval for this application, in milliseconds.  Default
	 * is every 30 seconds.
	 * 
	 * @param milliseconds
	 */
	public static void setUpdateInterval(long milliseconds) {
		updateInterval = milliseconds;
	}
	
	/**
	 * Get the AutoSaving update interval for this application, in milliseconds.
	 * 
	 * @return the interval, in milliseconds.
	 */
	public static long getUpdateInterval() {
		return updateInterval;
	}
	
}
