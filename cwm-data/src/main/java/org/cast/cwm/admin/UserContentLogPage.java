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

import lombok.extern.slf4j.Slf4j;
import net.databinder.models.hib.HibernateListModel;
import org.apache.wicket.Application;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.IAppConfiguration;
import org.cast.cwm.data.User;
import org.cast.cwm.data.UserContent;
import org.cast.cwm.data.builders.UserContentAuditQueryBuilder;
import org.cast.cwm.data.builders.UserCriteriaBuilder;
import org.cast.cwm.data.provider.AuditDataProvider;
import org.cast.cwm.data.provider.AuditTriple;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;

import java.util.ArrayList;
import java.util.List;

@AuthorizeInstantiation("RESEARCHER")
@Slf4j
public class UserContentLogPage extends LogPage {
	
	protected String urlPrefix;

	public UserContentLogPage(PageParameters parameters) {
		super(parameters);
		
		addFilterForm();

		AuditDataProvider<UserContent, DefaultRevisionEntity> provider = getDataProvider();
		
		List<IDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>> columns = makeColumns();
		DataTable<AuditTriple<UserContent,DefaultRevisionEntity>,String> table 
			= new DataTable<AuditTriple<UserContent,DefaultRevisionEntity>,String>("table", columns, provider, ITEMS_PER_PAGE);

		table.addTopToolbar(new HeadersToolbar<String>(table, provider));
		table.addBottomToolbar(new NavigationToolbar(table));
		table.addBottomToolbar(new NoRecordsToolbar(table, new Model<String>("No revisions found")));
		add(table);
		
		CSVDownload<AuditTriple<UserContent,DefaultRevisionEntity>> download = new CSVDownload<AuditTriple<UserContent,DefaultRevisionEntity>>(columns, provider);
		add(new ResourceLink<Object>("downloadLink", download));
		
		// Look for a configuration variable with site's URL, called either cwm.url or app.url.
		// If it is set, it is used to make URLs absolute in the downloaded file
		if (Application.get() instanceof CwmApplication) {
			IAppConfiguration config = CwmApplication.get().getConfiguration();
			urlPrefix = config.getString("cwm.url", config.getString("app.url", ""));
		}
	}

	protected void addFilterForm() {
		Form<Object> form = new Form<Object>("filter");
		add(form);
		addSiteFilter(form);
		addDateFilter(form);
		addOtherFilters(form);
	}
	
	protected void addOtherFilters(Form<Object> form) {
	}

	
	public AuditDataProvider<UserContent, DefaultRevisionEntity> getDataProvider() {
		UserContentAuditQueryBuilder qb = new UserContentAuditQueryBuilder();
		
		if (mFromDate.getObject()!=null && mToDate.getObject() != null) {
			log.debug("Considering events between {} and {}", mFromDate.getObject(), mToDate.getObject());
			qb.setMFromDate(mFromDate);
			qb.setMToDate(mToDate);
			
			// Model to return list of users to be considered -- based on choices made in Sites form
			qb.setMUsers(new AbstractReadOnlyModel<List<User>>() {
				private static final long serialVersionUID = 1L;
				@Override
				public List<User> getObject() {
					// If sites is set, create a list of users in those sites for the query.
					if (mShowSites.getObject().size() < numberOfSites) {
						UserCriteriaBuilder ucb = new UserCriteriaBuilder();
						ucb.setSites(mShowSites);
						List<User> list = new HibernateListModel<User>(User.class, ucb).getObject();
						log.trace("users: {}", list);
						return list;
					} else {
						log.trace("All sites checked");
						return null;
					}
				}
				
			});
		}

		return new AuditDataProvider<UserContent,DefaultRevisionEntity>(qb);	
	}

	protected List<IDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>> makeColumns() {
		List<IDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>> columns = new ArrayList<IDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>>(10);
		columns.add(new PropertyDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>("Rev ID", "info.id"));
		columns.add(new DateDataColumn<AuditTriple<UserContent, DefaultRevisionEntity>>("Rev Date", "info.revisionDate"));
		columns.add(new PropertyDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>("Rev Type", "type"));
		columns.add(new PropertyDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>("UC ID", "entity.id"));
		columns.add(new PropertyDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>("User", "entity.user.subjectId"));
		columns.add(new PropertyDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>("Type", "entity.dataType"));
		columns.add(new PropertyDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>("Title", "entity.title"));
		
		// What to do with the content column depends on the data type.
		columns.add(new AbstractDataColumn<AuditTriple<UserContent,DefaultRevisionEntity>>("Content") {
			@Override
			public void populateItem(
					Item<ICellPopulator<AuditTriple<UserContent, DefaultRevisionEntity>>> item,
					String componentId,
					IModel<AuditTriple<UserContent, DefaultRevisionEntity>> rowModel) {
				UserContent uc = rowModel.getObject().getEntity();
				if (!rowModel.getObject().getType().equals(RevisionType.DEL)) {
					if (uc.getDataType().name().equals("TEXT"))
						item.add(new Label(componentId, new PropertyModel<String>(rowModel, "entity.text")));
					else
						item.add(new ContentLinkPanel(componentId, rowModel.getObject().getEntity().getId(), rowModel.getObject().getInfo().getId()));					
				}
				else {
					item.add(new EmptyPanel(componentId));										
				}
			}
			@Override
			public String getItemString(IModel<AuditTriple<UserContent,DefaultRevisionEntity>> rowModel) {
				UserContent uc = rowModel.getObject().getEntity();
				if (uc.getDataType().name().equals("TEXT")) {
					return rowModel.getObject().getEntity().getText();
				} else {
					// output a text link for the spreadsheet
					Url path = Url.parse(urlFor(UserContentViewPage.class,getPageParameters(rowModel.getObject().getEntity().getId(), rowModel.getObject().getInfo().getId())).toString());
					return getRequestCycle().getUrlRenderer().renderFullUrl(path);
					// TODO, if we have a urlPrefix, replace Tomcat's idea of the URL with the given prefix.
				}
			}
			
		});

		return columns;

	}
	
	// PageParameters to use with UserContentViewPage.
	private PageParameters getPageParameters (long entityId, int revision) {
		PageParameters pp = new PageParameters();
		pp.add("id", entityId);
		pp.add("rev", revision);
		return pp;
	}
	
	
	private class ContentLinkPanel extends GenericPanel<Void> {

		public ContentLinkPanel(String id, long entityId, int revision) {
			super(id);
			add(new BookmarkablePageLink<Void>("link", UserContentViewPage.class, getPageParameters(entityId, revision)));			
		}

		
	}
	
}
