/*
 * Copyright 2011-2015 CAST, Inc.
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.OrderingCriteriaBuilder;
import net.databinder.models.hib.SortableHibernateProvider;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.ResponseData;
import org.cast.cwm.data.Site;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.ISiteService;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * 
 * This page enables searching of events by type, site, date or period related.
 * Events are then displayed in a table and may be downloaded.
 *
 */
@AuthorizeInstantiation("RESEARCHER")
public class EventLog extends AdminPage {

	protected IModel<List<String>> showEventTypesM;
	protected IModel<List<Site>> showSitesM;
	protected IModel<Date> fromDateM, toDateM;
	protected IModel<Boolean> inAPeriod;
	protected IModel<Boolean> showPermissionUsers;

	protected static final String eventDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final Logger log = LoggerFactory.getLogger(EventLog.class);
	
	private static final long serialVersionUID = 1L;

	@Inject
	protected IEventService eventService;
	
	@Inject
	protected ISiteService siteService;

	public EventLog(final PageParameters params) {
		super(params);
		setPageTitle("Event Log");

		addFilterForm();
		
		OrderingCriteriaBuilder builder = makeCriteriaBuilder();
		SortableHibernateProvider<Event> eventsprovider = makeHibernateProvider(builder);
		List<IDataColumn<Event>> columns = makeColumns();
		// Annoying to have to make a new List here; DataTable should use <? extends IColumn>.
		ArrayList<IColumn<Event>> colList = new ArrayList<IColumn<Event>>(columns);
		DataTable<Event> table = new DataTable<Event>("eventtable", colList, eventsprovider, 30);
		table.addTopToolbar(new HeadersToolbar(table, eventsprovider));
		table.addTopToolbar(new NavigationToolbar(table));
		table.addBottomToolbar(new NavigationToolbar(table));
		table.addBottomToolbar(new NoRecordsToolbar(table, new Model<String>("No events found")));
		add(table);

		CSVDownload<Event> download = new CSVDownload<Event>(columns, eventsprovider);
		add(new ResourceLink<Object>("downloadLink", download));

	}

	protected OrderingCriteriaBuilder makeCriteriaBuilder() {
		EventCriteriaBuilder eventCriteriaBuilder = new EventCriteriaBuilder();
		SingleSortState defaultSort = new SingleSortState();
		defaultSort.setSort(new SortParam("insertTime", false)); // Sort by Insert Time by default
		eventCriteriaBuilder.setSortState(defaultSort);
		return eventCriteriaBuilder;
	}

	protected SortableHibernateProvider<Event> makeHibernateProvider(OrderingCriteriaBuilder builder) {
		SortableHibernateProvider<Event> provider = new SortableHibernateProvider<Event>(Event.class, builder);
		provider.setWrapWithPropertyModel(false);
		return provider;
	}

	protected void addFilterForm() {
		Form<Object> form = new Form<Object>("filter");
		add(form);
		addEventTypeFilter(form);
		addDateFilter(form);
		addSiteFilter(form);
		addOtherFilters(form);
	}

	protected void addEventTypeFilter(Form<Object> form) {
		IModel<List<String>> allEventTypes = eventService.getEventTypes();
		List<String> eventTypes = new ArrayList<String>();
		eventTypes.addAll(allEventTypes.getObject());
		showEventTypesM = new ListModel<String>(eventTypes);
		form.add(new CheckBoxMultipleChoice<String>("type", showEventTypesM, allEventTypes));
	}

	protected void addDateFilter(Form<Object> form) {
		DateTime currentDateTime = new DateTime(new Date());
		toDateM = new Model<Date>(currentDateTime.toDate());
		fromDateM = new Model<Date>(currentDateTime.minusMonths(1).toDate());

		form.add(new DateTextField("from", fromDateM));
		form.add(new DateTextField("to", toDateM));		
	}
	
	protected void addSiteFilter(Form<Object> form) {
		IModel<List<Site>> allSites = siteService.listSites();
		List<Site> sites = new ArrayList<Site>();
		sites.addAll(allSites.getObject());
		showSitesM = new ListModel<Site>(sites);
		if (!allSites.getObject().isEmpty())
			form.add(new CheckBoxMultipleChoice<Site>("site", showSitesM, allSites, new ChoiceRenderer<Site>("name", "id")));
		else
			form.add(new WebMarkupContainer("site").setVisible(false));
	}
	
	protected void addOtherFilters(Form<Object> form) {
		inAPeriod = new Model<Boolean>(true);
		form.add(new CheckBox("showNoSite", inAPeriod));		

		showPermissionUsers = new Model<Boolean>(true);
		form.add(new CheckBox("showPermissionUsers", showPermissionUsers));
		
		
	}

	protected List<IDataColumn<Event>> makeColumns() {
		List<IDataColumn<Event>> columns = new ArrayList<IDataColumn<Event>>();
		
		columns.add(new PropertyDataColumn<Event>("EventID", "id", "id"));

		columns.add(new AbstractDataColumn<Event>("Date/Time", "insertTime") {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<Event>> cellItem, String componentId, IModel<Event> rowModel) {
				cellItem.add(DateLabel.forDatePattern(componentId, new PropertyModel<Date>(rowModel, "insertTime"), eventDateFormat));				
			}

			public String getItemString(IModel<Event> rowModel) {
				Event event = rowModel.getObject();
				Date insertTime = event.getInsertTime();
				return new SimpleDateFormat(eventDateFormat).format(insertTime); 
			}
		});
		
		columns.add(new PropertyDataColumn<Event>("User", "user.subjectId", "user.subjectId"));
		columns.add(new PropertyDataColumn<Event>("Event Type", "type", "type"));
		columns.add(new PropertyDataColumn<Event>("Details", "detail"));
		columns.add(new PropertyDataColumn<Event>("Page", "page"));
		columns.add(new AbstractDataColumn<Event>("Response") {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<Event>> cellItem, String componentId, IModel<Event> rowModel) {
				cellItem.add(new Label(componentId, getItemString(rowModel)));				
			}
			
			// TODO: this should do something more useful for audio, drawing, upload, and table responses.
			// The raw data display is not very usable inside a spreadsheet; and for audio and upload we don't display anything at all.
			// Perhaps we could include a link to display the item?  Or some metadata about it?
			public String getItemString(IModel<Event> rowModel) {
				if (!rowModel.getObject().hasResponses()) {
					return "";
				} else {
					Set<ResponseData> responseData = rowModel.getObject().getResponseData();
					if ((responseData == null) || (responseData.isEmpty())) {
						log.warn("Event {} claims to have responses, but none found", rowModel.getObject().getId());
						return "Missing Response Data";
					}
					StringBuffer result = new StringBuffer(256);
					if (responseData.size() > 1)
						result.append (String.format("[%d responses] ", responseData.size()));
					for (ResponseData r : responseData) {
						result.append(r.getResponse().getType().getName());
						result.append(" ");
						String text = r.getText();
						if (!Strings.isEmpty(text)) {
							result.append(text);
						}
					}
					return (result.toString());
				}
			}
			
		});

		return columns;
	}
	
	protected Date midnightEnd(Date olddate) {
		DateTime dateTime = new DateTime(olddate);
		DateTime adjustedDateTime
			= dateTime
				.plusDays(1)
				.withTimeAtStartOfDay();
		return adjustedDateTime.toDate();
	}

	protected Date midnightStart(Date olddate) {
		DateTime dateTime = new DateTime(olddate);
		DateTime adjustedDateTime
			= dateTime
				.withTimeAtStartOfDay();
		return adjustedDateTime.toDate();
	}

	public class EventCriteriaBuilder implements OrderingCriteriaBuilder, ISortStateLocator {

		private static final long serialVersionUID = 1L;
		@Getter @Setter private ISortState sortState;
;
		
		public void buildUnordered(Criteria criteria) {
			
			// Type check
			criteria.add(Restrictions.in("type", showEventTypesM.getObject()));

			// Site Check
			List<Site> siteList = showSitesM.getObject();
			criteria.createAlias("user", "user").createAlias("user.periods", "period", JoinType.LEFT_OUTER_JOIN);
			Disjunction siteRestriction = Restrictions.disjunction();
			if (!inAPeriod.getObject())
				siteRestriction.add(Restrictions.isEmpty("user.periods")); // Show users with no periods
			if (!siteList.isEmpty())
				siteRestriction.add(Restrictions.in("period.site",siteList)); // Show users with matching periods
			if (inAPeriod.getObject() && siteList.isEmpty()) {
				siteRestriction.add(Restrictions.idEq(-1L)); // Halt query early; don't show anyone.
				criteria.setMaxResults(0);
			}
			criteria.add(siteRestriction);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);  // Remove duplicate rows as a result of the INNER JOIN
			
			if (fromDateM.getObject()!=null && toDateM.getObject() != null) {
				Date startDate = midnightStart(fromDateM.getObject());
				Date endDate = midnightEnd(toDateM.getObject());
				log.debug("Considering events between {} and {}", startDate, endDate);
				criteria.add(Restrictions.between("insertTime", startDate, endDate));
			}

			//set permission check
			if (showPermissionUsers.getObject()) {
				criteria.add(Restrictions.eq("user.permission", true));
			}
			
			// Also load ResponseData elements in the same query, to avoid thousands of subsequent queries.
			criteria.setFetchMode("responseData", FetchMode.JOIN);

		}

		public void buildOrdered(Criteria criteria) {
			buildUnordered(criteria);
			SortParam sort = ((SingleSortState) getSortState()).getSort();
			if (sort != null) {
				if (sort.isAscending())
					criteria.addOrder(Order.asc(sort.getProperty()).ignoreCase());
				else
					criteria.addOrder(Order.desc(sort.getProperty()).ignoreCase());
			}
		}
	}
}
