/*
 * Copyright 2011-2013 CAST, Inc.
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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.event.UpdateCurrentPeriodMessage;
import org.cast.cwm.service.ICwmSessionService;

import com.google.inject.Inject;

public class PeriodChoicePanel extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	ICwmSessionService sessionService;
	

	public PeriodChoicePanel(String id) {
		super(id);
	}

	@Override
	protected void onBeforeRender() {
		addOrReplace(new SelectForm("selectForm"));
		super.onBeforeRender();
	}

	private class SelectForm extends Form<Void> {

		private static final long serialVersionUID = 1L;

		public SelectForm(String id) {
			super(id);
			
			add(new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this)).setOutputMarkupId(true));
			PeriodChoice periodSelection = new PeriodChoice("periodChoice", getCurrentPeriodModel(), getPeriodsForUser());
			add(periodSelection);		
			
			ViewSubmitLink view = new ViewSubmitLink("view", this);
			add(view);
		}

		private PropertyModel<List<Period>> getPeriodsForUser() {
			System.err.println("User: " + sessionService.getUserModel());
			return new PropertyModel<List<Period>>(sessionService.getUserModel(), "periodsAsList");
		}

		@SuppressWarnings("unchecked")
		private IModel<Period> getSelectedPeriodModel() {
			return (IModel<Period>) get("periodChoice").getDefaultModel();
		}
		
		
		private class ViewSubmitLink extends AjaxSubmitLink {

			private static final long serialVersionUID = 1L;

			public ViewSubmitLink(String id, Form<Void> form) {
				super(id, form);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				// update the current period if not already set
				if (getSelectedPeriodModel() != getCurrentPeriodModel()) {
					sessionService.setCurrentPeriodModel(getSelectedPeriodModel());
				}
				
				// send out the value of the current measure type
				send(getParent(), Broadcast.BUBBLE, new UpdateCurrentPeriodMessage(target));
				super.onSubmit(target, form);
			}			
		}		
	}

	private IModel<Period> getCurrentPeriodModel() {
		return 	sessionService.getCurrentPeriodModel();
	}


}
