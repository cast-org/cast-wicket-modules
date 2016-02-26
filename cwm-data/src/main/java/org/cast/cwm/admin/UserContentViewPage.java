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
package org.cast.cwm.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.UserContent;
import org.cast.cwm.data.models.HibernateAuditObjectModel;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.service.IUserContentViewerFactory;

import com.google.inject.Inject;

/**
 * Display a UserContent object.
 */
@AuthorizeInstantiation("RESEARCHER")
public class UserContentViewPage extends AdminPage {
	
	@Inject
	IUserContentViewerFactory viewerFactory;
	
	@Inject
	ICwmService cwmService;
	
	private static final long serialVersionUID = 1L;

	public UserContentViewPage(PageParameters parameters) {
		super(parameters);
		setPageTitle("Content Viewer");
		
		long ucId  = parameters.get("id").toLong();
		int rev  = parameters.get("rev").toInt();
		
		IModel<UserContent> mUserContent = new HibernateAuditObjectModel<UserContent>(UserContent.class, ucId, rev);
		
		if (viewerFactory.canHandle(mUserContent)) {
			add(viewerFactory.makeContentViewer("display", mUserContent, 500, 500));
			add(new Label("message", String.format("Displaying content id %d at revision %d", ucId, rev)));
		} else {
			add(new EmptyPanel("display"));
			add(new Label("message", String.format("Unable to display this content (id=%d,rev=%d)", ucId, rev)));
		}
		
	}
	
}
