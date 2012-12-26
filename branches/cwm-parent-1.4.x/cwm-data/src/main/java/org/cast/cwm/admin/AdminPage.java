/*
 * Copyright 2011 CAST, Inc.
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

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.CwmApplication;

/**
 * Base for all Admin Pages.
 * 
 * @author jbrookover
 *
 */
public abstract class AdminPage extends WebPage implements IHeaderContributor {
	
	public static final ResourceReference admincss = new ResourceReference(AdminPage.class, "admin.css");
	
	@Getter protected String pageTitle = CwmApplication.get().getAppAndInstanceId() + " :: Default Page Title";

	public AdminPage (final PageParameters parameters) {
		super(parameters);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(new AdminHeaderPanel("header"));
	}

	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(admincss);
	}
	
	public void setPageTitle(String pageTitle) {
		this.pageTitle = CwmApplication.get().getAppAndInstanceId() + " :: " + pageTitle;
	}

}
