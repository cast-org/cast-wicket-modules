package org.cast.cwm.data.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * @AjaxMessage sent when the session's current Period is changing.
 *
 */
public class UpdateCurrentPeriodMessage extends AjaxMessage {

	public UpdateCurrentPeriodMessage(AjaxRequestTarget target) {
		super(target);
	}

}
