/*
 * Copyright 2011-2018 CAST, Inc.
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
package org.cast.cwm.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.pages.BrowserInfoForm;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;

/**
 * A stateless form component that will, as a side effect, gather extended browser information.
 * Designed for use as a base class for a login form that all users will pass through; client
 * information is gathered and submitted as part of the login process.
 *
 * Based on the standard Wicket {@see org.apache.wicket.markup.html.pages.BrowserInfoForm},
 * but avoids the need for a redirect and stealthy form submission.
 *
 * @author bgoldowsky
 *
 * @param <T> the model type of the form.  Can be void.
 */
@Slf4j
public class BrowserInfoGatheringForm<T> extends StatelessForm<T> {

	public BrowserInfoGatheringForm(String id) {
		this(id, null);
	}

	public BrowserInfoGatheringForm(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		// This javascript will create HTML input fields for all browser info items, and populate them on page load.
		response.render(JavaScriptHeaderItem.forReference(BrowserInfoForm.JS));
		response.render(OnLoadHeaderItem.forScript(getOnLoadScript()));
	}

	/**
	 * Javascript to create a hidden container in the form and populate it with the required form fields.
	 * @return Javascript string to be run after page load.
	 */
	protected String getOnLoadScript() {
		String hiddenDivId = getHiddenDivId();
		return String.format(
				"var div = document.createElement('div');" +
						"div.id='%s';" +
						"div.style.display='none';" +
						"document.getElementById('%s').appendChild(div);" +
						"Wicket.BrowserInfo.populateFields('%s');",
				hiddenDivId, getMarkupId(), hiddenDivId);
	}

	// ID of the hidden container that will be created inside the form.
	protected String getHiddenDivId() {
		return "browserInfoContainer";
	}

	/**
	 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
	 */
	@Override
	protected void onSubmit() {
		WebSession session = (WebSession) getSession();
		WebClientInfo ci = session.getClientInfo();
		if (ci == null) {
			ci = new WebClientInfo(getRequestCycle());
			session.setClientInfo(ci);
		}
		ci.getProperties().read(getRequest().getPostParameters());
		log.debug("Set client properties.  utcOffset={}", ci.getProperties().getUtcOffset());
	}

}
