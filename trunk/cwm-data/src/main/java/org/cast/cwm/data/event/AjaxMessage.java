package org.cast.cwm.data.event;

import lombok.Getter;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * Superclass for all Messages that are tied to Ajax events,
 * and thus have an AjaxRequestTarget.  A Message in CWM
 * is the payload of an {@link org.apache.wicket.event.IEvent}
 *
 */
public class AjaxMessage {

	@Getter
	private AjaxRequestTarget target;

	public AjaxMessage(AjaxRequestTarget target) {
		this.target = target;
	}

}
