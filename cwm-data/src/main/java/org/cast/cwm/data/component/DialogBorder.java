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
package org.cast.cwm.data.component;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.cast.cwm.components.ShyLabel;
import org.cast.cwm.service.IEventService;

import com.google.inject.Inject;
/**
 * A Styled modal dialog popup box.  This provides a default style and markup, but applications
 * can override these.  The title of the dialog box is set by this component's model, while
 * its content is the body of the border.
 * 
 * This is similar to Dialog, but is a border so that it can enforce consistent markup of dialogs.
 * This may replace AbstractStyledDialog at some point since it is more flexible.
 * 
 * @author bgoldowsky
 * 
 * TODO: determine if we should keep styleReferences or use overriding to set styles
 * TODO: somehow make default stylesheet not interfere so much with application stylesheets
 *
 * @param <T>
 */
public class DialogBorder extends Border implements IHeaderContributor {

	private static final long serialVersionUID = 1L;
	
	private static final PackageResourceReference JS_REFERENCE = new JavaScriptResourceReference(DialogBorder.class, "DialogBorder.js");
	private static final PackageResourceReference BLOCKING_CSS_REFERENCE = new PackageResourceReference(DialogBorder.class, "modal_blocking.css");
	
	@Getter @Setter
	private boolean logEvents = true;
	
	@Getter @Setter
	private String eventCode = "dialog:open";
	
	@Getter @Setter
	private String eventDetail;
	
	@Getter @Setter
	private String pageName;
		
	@Getter @Setter
	protected boolean clickBkgToClose = false;
	
	@Getter @Setter
	private boolean showCloseLink = true;

	@Getter @Setter
	private boolean showMoveLink = false;

	@Getter @Setter
	private boolean autoSave = false; // autosave on close

	/**
	 * If true, DOM contents will be erased by the close javascript.
	 * Useful if, for instance, there are videos or other active components in the modal that must be stopped.
	 */
	@Getter @Setter
	protected boolean emptyOnClose = false;
	
	@Getter @Setter
	protected Integer zIndex;
	
	@Getter @Setter
	protected boolean masking = true;

	@Getter
	protected WebMarkupContainer contentContainer;
	
	@Inject
	private IEventService eventService;
	
	/**
	 * The ancestor of this DialogBorder thats client-side markup will be moved, via Javascript, 
	 * to the end of the page.  This can solve some CSS display issues.  If null, the 
	 * contentContainer will be moved instead, possibly separating the client-side markup of 
	 * this dialog from its wicket ancestry.
	 */
	@Getter @Setter
	protected WebMarkupContainer moveContainer;

	@Getter
	protected WebMarkupContainer overlay;
	
	// Holds application specified styles for this border.
	private static List<ResourceReference> styleReferences;
	
	private AbstractDefaultAjaxBehavior openEventBehavior;
	
	
	/**
	 * When the dialog is closed, the DialogBorder will
	 * attempt to return focus to a component based on this jQuery selector.
	 */
	@Getter @Setter
	protected String focusOverrideSelector;

	public DialogBorder (String id) {
		this(id, "");
	}
	
	public DialogBorder (String id, String title) {
		this(id, new Model<String>(title));
	}
	
	public DialogBorder (String id, final IModel<String> model) {
		super(id, model);
		setRenderBodyOnly(true);
		
		addContentContainer();
		contentContainer.setOutputMarkupId(true);
		contentContainer.add(AttributeModifier.replace("tabindex", "-1"));
		
		addTitle(contentContainer);
		addControls(contentContainer);
		addBody(contentContainer);
		
		addOverlay();
		
		openEventBehavior = new AbstractDefaultAjaxBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				logOpenEvent(target);
			}
		};
		add(openEventBehavior);
	}
	
	/**
	 * Log an event when the dialog is opened.
	 * 
	 * @param target
	 */
	protected void logOpenEvent(AjaxRequestTarget target) {
		eventService.storeEvent(this, eventCode, eventDetail);
	}
	
	
	
	/**
	 * Add the border's body.
	 * @param container main content container
	 */
	protected void addBody (WebMarkupContainer container) {
		container.add(getBodyContainer());
	}
	
	/**
	 * Create and add the overall container for the dialog.
	 */
	protected void addContentContainer() {
		addToBorder (contentContainer = new WebMarkupContainer("contentContainer") {
		    private static final long serialVersionUID = 1L;
			@Override
		    protected void onComponentTag(ComponentTag tag) {
		    	super.onComponentTag(tag);
		    	tag.put("style", "display:none;" + (zIndex == null ? "" : "z-index: " + zIndex.toString()));
		    }
		});
	}
	
	/**
	 * Add a title component for the dialog.
	 * @param container
	 */
	protected void addTitle(WebMarkupContainer container) {
		container.add(new ShyLabel("title", getModel()));
	}
	
	/**
	 * Add any buttons or controls to the window.
	 * By default, adds a close button.
	 * @param container
	 */
	protected void addControls(WebMarkupContainer container) {
		addMoveLink(container);
		addCloseLink(container);
	}

	/** Add a control that allows dragging the dialog (or moving it with the keyboard).
	 * 
	 * @param container
	 */
	protected void addMoveLink(WebMarkupContainer container) {
		container.add (new WebMarkupContainer("moveWindowLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				// Blocking modals should not show the move link since (a) they are blocking the
				// content anyway, and (b) it does not work with current JS (z-index mismatch).
				return showMoveLink && !masking;
			}
		});
	}

	/**
	 * Add a close link to the dialog.
	 * @param container
	 */
	protected void addCloseLink(WebMarkupContainer container) {
		WebMarkupContainer link = new WebMarkupContainer("closeWindowLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return showCloseLink;
			}
		};
		container.add(link);
		link.add(getClickToCloseBehavior());
	}
	
	/**
	 * Create and add the window-masking overlay, if appropriate.
	 */
	protected void addOverlay() {

		overlay = new WebMarkupContainer("overlay") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(DialogBorder.this.masking);
				if (DialogBorder.this.clickBkgToClose)
					add(DialogBorder.this.getClickToCloseBehavior());
			}
		};
		addToBorder(overlay);
		overlay.setOutputMarkupId(true);
	}
	
    /**
     * Open using the current Ajax context.
     * @param target
     */
    public void open(AjaxRequestTarget target) {
        target.appendJavaScript(getOpenString());
    }

    /**
     * Close using the current Ajax context.
     * @param target
     */
    public void close(AjaxRequestTarget target) {
        target.appendJavaScript(getCloseString());
    }

    /**
     * Return a Behavior which when applied to a Component will add an "onclick"
     * event handler that will open this Dialog.
     * @return
     */
    public Behavior getClickToOpenBehavior() {
        return new Behavior() {
        	
			private static final long serialVersionUID = 1L;

			@Override
            public void onComponentTag(Component component, ComponentTag tag) {
                tag.put("onclick", getOpenString());
            }
        };
    }

    /**
     * Return a Behavior which when applied to a Component will add an "onlick"
     * event handler that will close this Dialog.
     * @return
     */
    public Behavior getClickToCloseBehavior() {
        return new Behavior() {

			private static final long serialVersionUID = 1L;

			@Override
            public void onComponentTag(Component component, ComponentTag tag) {
                tag.put("onclick", getCloseString()+"return false;");
            }
        };
    }

    /**
     * Return Javascript to run to open the dialog.
     * @return
     */
    public String getOpenString() {
        return getOpenString(true);
    }
    
    public String getOpenString(boolean storeCallingButton) {
    	if (!isEnabled()) {
            return "";
        }
        StringBuffer result = new StringBuffer();
        if (masking) {
        	result.append("$('#" + overlay.getMarkupId() + "').show();");
        	result.append("$('#" + overlay.getMarkupId() + "').height($(document).height()); ");
        }
        
        result.append("$('#" + contentContainer.getMarkupId() + "').show();");
        result.append(getPositioningString());
        result.append("if (typeof(modalInit)==='function') modalInit(); ");
        result.append("DialogBorder.focusDialog('" + contentContainer.getMarkupId() + "', " + (storeCallingButton? "true" : "false") + ");");
        if (logEvents)
        	result.append("Wicket.Ajax.get({u:'" + openEventBehavior.getCallbackUrl() + "'});");
        return result.toString();	
    }
    
    /**
     * Return the Javascript that will position the dialog when it is opened.
     * By default centers the dialog in the window, but you can override this
     * method to change the behavior.
     * @return String of Javascript.
     */
    protected String getPositioningString() {
    	return "$('#" + contentContainer.getMarkupId() + "').center(); ";
    }

    
    /**
     * Return javascript that will be run when the dialog is closed.
     * @return
     */
    public String getCloseString() {
    	return getCloseString(true);
    }

    /**
     * Return javascript that will be run when the dialog is closed.  Optional 
     * parameter lets you prevent this method from changing focus.  This can be
     * useful if you just want your dialog to disappear silently (e.g. a loading
     * dialog).  If you do not adjust focus here, you should adjust focus elsewhere
     * to maintain accessibility.
     * 
     * @param returnFocus
     * @return
     */
    public String getCloseString(boolean returnFocus) {
    	StringBuffer result = new StringBuffer();
        if (autoSave)
        	result.append("AutoSaver.autoSaveMaybeSave(null);");
        result.append(String.format("$('#%s').hide()%s;", 
        		contentContainer.getMarkupId(), 
        		emptyOnClose ? ".empty()" : ""));
        if (masking)
        	result.append("$('#" + overlay.getMarkupId() + "').hide();");
        if (returnFocus)
        	result.append(String.format("DialogBorder.focusButton('%s', '%s');",        			
        			contentContainer.getMarkupId(),
        			focusOverrideSelector == null ?  "" : focusOverrideSelector));
        return result.toString();
    }

	@Override
	public void renderHead(final IHeaderResponse response) {
		renderJSinclusions(response);
        renderCSSinclusions(response);

        // Move dialog components out of any containers on the page so that page CSS doesn't interfere with proper display
        // and so that they display on top of other content in older IE versions that don't respect z-index.
        if (moveContainer != null && moveContainer.contains(this, true)) {
        	moveContainer.setOutputMarkupId(true);
        	response.render(OnDomReadyHeaderItem.forScript("$('#" + moveContainer.getMarkupId() + "').detach().appendTo('body');"));
        } else {
        	response.render(OnDomReadyHeaderItem.forScript(
				"$('#" + contentContainer.getMarkupId() + "').detach().appendTo('body');"
				+ (masking ? "$('#" + overlay.getMarkupId() + "').detach().appendTo('body');" : "")));
        }
	}
	
	protected void renderJSinclusions(final IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(JS_REFERENCE));
	}

	protected void renderCSSinclusions(final IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(BLOCKING_CSS_REFERENCE));
		if (styleReferences == null)
			response.render(CssHeaderItem.forReference(new PackageResourceReference(DialogBorder.class, "modal.css")));
		else
			for (ResourceReference ref : styleReferences)
				response.render(CssHeaderItem.forReference(ref));
	}
	
	/**
	 * Set the list of CSS style resource references that this dialog should
	 * render.  Use this to override the default styles.
	 * 
	 * @param list
	 */
	public static void setStyleReferences(List<ResourceReference> list) {
		styleReferences = list;
	}

	/**
	 * Gets model.  The model of a DialogBorder is used to display the title.
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public final IModel<String> getModel()
	{
		return (IModel<String>)getDefaultModel();
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public final void setModel(IModel<String> model)
	{
		setDefaultModel(model);
	}

	/**
	 * Gets model object
	 * 
	 * @return model object
	 */
	public final String getModelObject()
	{
		return getModel().getObject();
	}

	/**
	 * Sets model object
	 * 
	 * @param object
	 */
	public final void setModelObject(String object)
	{
		setDefaultModelObject(object);
	}

}
