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
package org.cast.cwm.components;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.io.IClusterable;

/**
 * A stateless form component that will, as a side effect, gather extended browser information.
 * 
 * Based on the standard {@see org.apache.wicket.markup.html.pages.BrowserInfoForm}, but allows
 * other form fields to be added so that you can gather the browser information as part of,
 * say, a login form rather than as a separate page where the browser has to be redirected.
 * 
 * @author bgoldowsky
 *
 * @param <T> the model type of the form.  Can be void.
 */
public class BrowserInfoGatheringForm<T> extends StatelessForm<T> {

	private static final long serialVersionUID = 1L;

	protected ClientPropertiesBean bean = new ClientPropertiesBean();

	public BrowserInfoGatheringForm(String id) {
		this(id, null);
	}

	public BrowserInfoGatheringForm(String id, IModel<T> model) {
		super(id, model);

		add(new HiddenField<String>("navigatorAppName", new PropertyModel<String>(this, "bean.navigatorAppName")));
		add(new HiddenField<String>("navigatorAppVersion", new PropertyModel<String>(this, "bean.navigatorAppVersion")));
		add(new HiddenField<String>("navigatorAppCodeName", new PropertyModel<String>(this, "bean.navigatorAppCodeName")));
		add(new HiddenField<Boolean>("navigatorCookieEnabled", new PropertyModel<Boolean>(this, "bean.navigatorCookieEnabled")));
		add(new HiddenField<Boolean>("navigatorJavaEnabled", new PropertyModel<Boolean>(this, "bean.navigatorJavaEnabled")));
		add(new HiddenField<String>("navigatorLanguage", new PropertyModel<String>(this, "bean.navigatorLanguage")));
		add(new HiddenField<String>("navigatorPlatform", new PropertyModel<String>(this, "bean.navigatorPlatform")));
		add(new HiddenField<String>("navigatorUserAgent", new PropertyModel<String>(this, "bean.navigatorUserAgent")));
		add(new HiddenField<String>("screenWidth", new PropertyModel<String>(this, "bean.screenWidth")));
		add(new HiddenField<String>("screenHeight", new PropertyModel<String>(this, "bean.screenHeight")));
		add(new HiddenField<String>("screenColorDepth", new PropertyModel<String>(this, "bean.screenColorDepth")));
		add(new HiddenField<String>("utcOffset", new PropertyModel<String>(this, "bean.utcOffset")));
		add(new HiddenField<String>("utcDSTOffset", new PropertyModel<String>(this, "bean.utcDSTOffset")));
		add(new HiddenField<String>("browserWidth", new PropertyModel<String>(this, "bean.browserWidth")));
		add(new HiddenField<String>("browserHeight", new PropertyModel<String>(this, "bean.browserHeight")));
		add(new HiddenField<String>("hostname", new PropertyModel<String>(this, "bean.hostname")));
	}

	/**
	 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
	 */
	@Override
	protected void onSubmit() {
		RequestCycle requestCycle = getRequestCycle();
		WebSession session = (WebSession) getSession();
		ClientInfo ci = session.getClientInfo();

		if (ci == null) {
			ci = new WebClientInfo(requestCycle);
			getSession().setClientInfo(ci);
		} else {
			if (!(ci instanceof WebClientInfo))
				throw new RuntimeException("ClientInfo is not of expected type");
		}
		WebClientInfo clientInfo = (WebClientInfo) ci;

		ClientProperties properties = clientInfo.getProperties();
		bean.merge(properties);
	}

	public ClientPropertiesBean getBean() {
		return bean;
	}

	@Getter
	@Setter
	protected static class ClientPropertiesBean {

		private String navigatorAppName;
		private String navigatorAppVersion;
		private String navigatorAppCodeName;
		private Boolean navigatorCookieEnabled = Boolean.FALSE;
		private Boolean navigatorJavaEnabled = Boolean.FALSE;
		private String navigatorLanguage;
		private String navigatorPlatform;
		private String navigatorUserAgent;
		private String screenHeight;
		private String screenWidth;
		private String screenColorDepth;
		private String utcOffset;
		private String utcDSTOffset;
		private String browserWidth;
		private String browserHeight;
		private String hostname;

		public void merge(ClientProperties properties) {
			properties.setNavigatorAppName(navigatorAppName);
			properties.setNavigatorAppVersion(navigatorAppVersion);
			properties.setNavigatorAppCodeName(navigatorAppCodeName);
			properties.setNavigatorCookieEnabled(navigatorCookieEnabled);
			properties.setNavigatorLanguage(navigatorLanguage);
			properties.setNavigatorPlatform(navigatorPlatform);
			properties.setNavigatorUserAgent(navigatorUserAgent);
			properties.setScreenWidth(getInt(screenWidth));
			properties.setScreenHeight(getInt(screenHeight));
			properties.setBrowserWidth(getInt(browserWidth));
			properties.setBrowserHeight(getInt(browserHeight));
			properties.setScreenColorDepth(getInt(screenColorDepth));
			properties.setUtcOffset(utcOffset);
			properties.setUtcDSTOffset(utcDSTOffset);
			properties.setHostname(hostname);
		}

		private int getInt(String value) {
			int intValue = -1;
			try {
				intValue = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// Do nothing
			}
			return intValue;
		}

	}

}
