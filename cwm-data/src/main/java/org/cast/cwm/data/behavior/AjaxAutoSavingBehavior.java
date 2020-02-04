/*
 * Copyright 2011-2020 CAST, Inc.
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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.wicket.ClassAttributeModifier;
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
import org.cast.cwm.service.ICwmService;

import java.util.Set;

/**
 * This behavior will submit a form at regular intervals.  Any HTML element
 * with class='autosave_info' will display the status of these automatic
 * saves.
 * 
 * @author jbrookover
 *
 */
public class AjaxAutoSavingBehavior extends AjaxFormSubmitBehavior {

	@Inject
	private ICwmService cwmService;

	private static long updateInterval = 30000; 

	private static final String AUTOSAVE_EVENT = "autosave";
	
	private static final PackageResourceReference AUTOSAVING_JAVASCRIPT
			= new PackageResourceReference(AjaxAutoSavingBehavior.class, "AjaxAutoSavingBehavior.js");

	/**
	 * No-arg constructor; can be used when attaching to a component INSIDE the form.
	 */
	public AjaxAutoSavingBehavior() {
		super(AUTOSAVE_EVENT);
	}
	
	/**
	 * Constructor including the form.
     * Used when attaching to a component outside the form, or to the form itself.
	 * 
	 * @param form the form to be autosaved
	 */
	public AjaxAutoSavingBehavior(Form<?> form) {
		super(form, AUTOSAVE_EVENT);
	}

    @Override
    protected void onBind() {
        super.onBind();
    }

    @Override
	public void onConfigure(Component component) {
		getForm().setOutputMarkupId(true);
		getForm().add(new ClassAttributeModifier() {
            @Override
            protected Set<String> update(Set<String> classes) {
                classes.add("ajaxAutoSave");
                return classes;
            }
        });
		super.onConfigure(component);
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

		// The Javascript makes use of Loglevel for debugging
		response.render(JavaScriptHeaderItem.forReference(cwmService.getLoglevelJavascriptResourceReference()));
		
		// Run once to initialize
		response.render(JavaScriptHeaderItem.forReference(AUTOSAVING_JAVASCRIPT));
		response.render(JavaScriptHeaderItem.forScript("AutoSaver.setup(" + updateInterval + ");",
                "AjaxAutoSavingBehaviorSetup"));

		String callback = getBeforeSaveCallbackJavascript();
		if (!Strings.isNullOrEmpty(callback))
            response.render(JavaScriptHeaderItem.forScript(
                    String.format("AutoSaver.addOnBeforeSaveCallBack(function() { %s; });", callback),
                    "AjaxAutoSavingCallbackSetup"));

		// Run on each render to make sure page links trigger a save first
        response.render(OnDomReadyHeaderItem.forScript("AutoSaver.makeLinksSafe();"));

		// Run on each render to register this Form's default values and call-back URL with the AutoSaver
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("AutoSaver.addForm('%s', '%s');", getForm().getMarkupId(), this.getCallbackUrl())));
	}

    /**
     * Define Javascript code that will be executed before the form is checked for changes.
     * When using tools such as WYSIWYG editors or drawing tools, there is often code needed to pull
     * out the information from the fancy widget and put it in a regular form field for saving.
     * Override this method in those cases to return the appropriate code.
     *
     * @return Javascript code, or null if none
     */
	protected String getBeforeSaveCallbackJavascript() {
	    return null;
    }

	/**
	 * Called when the form auto-submits itself.  By default, this does nothing and presumes
	 * the form should process silently in the background.  Override this method to provide
	 * additional ajax changes to the page.
	 * 
	 * @param target AJAX request
	 */
	protected void onAutoSave(AjaxRequestTarget target) {
	}
	
	/**
	 * Set the AutoSaving update interval for this application, in milliseconds.  Default
	 * is every 30 seconds.
	 * 
	 * @param milliseconds new interval, in milliseconds
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
