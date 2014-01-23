package org.cast.cwm.data.component;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.IUserPreferenceService;

import com.google.inject.Inject;

/**
 * 
 * A toggle link button that can show/hide a related div.  The toggle link has 
 * the classes "linkToggle linkToggleBtn_xxx" where xxx is a unique toggle id.
 * The related div must have the class "targetToggle linkToggleBox_xxx".
 *  
 * @author lynnmccormack
 *
 */
public class CollapseToggleLink extends AjaxLink<Void> {

	private static final long serialVersionUID = 1L;

	@Inject
	protected IEventService eventService;
	
	@Inject
	protected IUserPreferenceService userPreferenceService;
	
	@Inject
	protected ICwmSessionService sessionService;

	@Getter @Setter
	// a state of true = open; set to false for closed state
	protected Boolean toggleState = true;

	@Setter
	// unique id for this toggle area - cannot contain underscore
	protected String toggleId;
	
	@Setter
	// the user preference to store the state of this toggle area
	protected String togglePreferenceId;

	@Setter
	// flag indicating toggle event should be logged
	protected Boolean logEvent = true;
	
	@Getter @Setter
	// additional event details
	protected String eventDetail = "";
	
	@Getter @Setter
	// event page name
	protected String eventPageName = "";
	

	
	public CollapseToggleLink(String id, String toggleId) {
		this(id, toggleId, "");
	}

	public CollapseToggleLink(String id, String toggleId, String togglePreferenceId) {
		super(id);
		this.toggleId = toggleId;
		this.togglePreferenceId = togglePreferenceId;
		
		setOutputMarkupId(true);
		add(new AttributeAppender("class", Model.of("linkToggle"), " "));
		add(new AttributeAppender("class", Model.of("linkToggleBtn_" + toggleId), " "));
	
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		// set the toggle state from the saved state otherwise use the toggleState
		if (togglePreferenceId != null || !togglePreferenceId.isEmpty()) {
			initToggleState();
		}
		add(new ClassAttributeModifier("open", !toggleState));
	}
	

	@Override
	public void onClick(AjaxRequestTarget target) {
		toggleState = !toggleState;
		userPreferenceService.setUserPreferenceBoolean(sessionService.getUserModel(), togglePreferenceId, toggleState);	
		if (logEvent) {
			logToggleEvent();
		}
	}

	protected void initToggleState() {
		IModel<User> userModel = sessionService.getUserModel();
		Boolean rememberedState = userPreferenceService.getUserPreferenceBoolean(userModel, togglePreferenceId);
		if (rememberedState != null)
			toggleState = rememberedState;
	}

	private void logToggleEvent() {
		String details = toggleId + "=" + (toggleState ? "open" : "close");
		if (!eventDetail.isEmpty())
			details = details + ", " + eventDetail;
		eventService.saveEvent("toggleArea", details, eventPageName);
	}
}