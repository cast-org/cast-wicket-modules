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
package org.cast.cwm.admin;

import lombok.Getter;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.IAppConfiguration;

import com.google.inject.Inject;
import org.cast.cwm.figuration.service.IFigurationService;

/**
 * Base for all Admin Pages.
 * 
 * @author jbrookover
 *
 */
public abstract class AdminPage extends WebPage implements IHeaderContributor {

	public static final ResourceReference admincss = new PackageResourceReference(AdminPage.class, "admin.css");
	
	@Getter protected String pageTitle;
	
	@Inject
	private IAppConfiguration configuration;

	@Inject
	private IFigurationService figurationService;

	public AdminPage (final PageParameters parameters) {
		super(parameters);
		
		pageTitle = getDefaultPageTitle();
		
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(new AdminHeaderPanel("header"));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		figurationService.addFigurationHeaderItems(response);
		response.render(CssHeaderItem.forReference(admincss));
	}
	
	protected String getDefaultPageTitle() {
		return getPageTitleBase() + "Default Page Title";
	}
	
	protected String getPageTitleBase() {
		StringBuffer t = new StringBuffer();
		if (Application.get() instanceof CwmApplication) {
			t.append(CwmApplication.get().getAppId());
			t.append("/"); 
		}
		t.append(configuration.getString("instanceId", "?"));
		return t.toString();
	}
	
	public void setPageTitle(String pageTitle) {
		this.pageTitle = getPageTitleBase() + pageTitle;
	}

}
