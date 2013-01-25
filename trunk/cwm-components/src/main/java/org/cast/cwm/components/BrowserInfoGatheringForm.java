package org.cast.cwm.components;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.pages.BrowserInfoForm.ClientPropertiesBean;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.ClientInfo;

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
		RequestCycle rc = getRequestCycle();
		if (rc instanceof WebRequestCycle) {
			WebRequestCycle requestCycle = (WebRequestCycle) rc;
			WebSession session = (WebSession)getSession();
			ClientInfo ci = session.getClientInfo();

			if (ci == null) {
				ci = new WebClientInfo(requestCycle);
				getSession().setClientInfo(ci);
			} else {
				if (!(ci instanceof WebClientInfo))
					throw new RuntimeException ("ClientInfo is not of expected type");
			}
			WebClientInfo clientInfo = (WebClientInfo) ci;
			
			ClientProperties properties = clientInfo.getProperties();
			bean.merge(properties);
		}

	}

	public ClientPropertiesBean getBean() {
		return bean;
	}


}
