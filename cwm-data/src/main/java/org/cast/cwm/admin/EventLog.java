/*
 * Copyright 2011-2017 CAST, Inc.
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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.databinder.models.hib.ICriteriaBuilder;
import net.databinder.models.hib.SortableHibernateProvider;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.IEventType;
import org.cast.cwm.data.Event;
import org.cast.cwm.data.Site;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.ISiteService;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.joda.time.DateTime;

import com.google.inject.Inject;

/**
 * 
 * This page enables searching of events by type, site, date or period related.
 * Events are then displayed in a table and may be downloaded.
 *
 */
@AuthorizeInstantiation("RESEARCHER")
@Slf4j
public class EventLog extends AdminPage {

	protected int numberOfEventTypes;
	
	protected IModel<List<IEventType>> showEventTypesM;
	protected int numberOfSites;
	protected IModel<List<Site>> showSitesM;

	protected IModel<Date> fromDateM, toDateM;
	protected IModel<Boolean> inAPeriod;
	protected IModel<Boolean> showPermissionUsers;

	@Inject
	private IEventService eventService;
	
	@Inject
	private ISiteService siteService;

	public EventLog(final PageParameters params) {
		super(params);
		setPageTitle("Event Log");

		addFilterForm();
		
		ICriteriaBuilder builder = makeCriteriaBuilder();
		SortableHibernateProvider<Event> eventsprovider = makeHibernateProvider(builder);
		List<IDataColumn<Event>> columns = makeColumns();
		DataTable<Event,String> table = new DataTable<>("eventtable", columns, eventsprovider, 30);
		table.addTopToolbar(new HeadersToolbar<>(table, eventsprovider));
		table.addTopToolbar(new NavigationToolbar(table));
		table.addBottomToolbar(new NavigationToolbar(table));
		table.addBottomToolbar(new NoRecordsToolbar(table, new Model<>("No events found")));
		add(table);

		CSVDownload<Event> download = new CSVDownload<>(columns, eventsprovider);
		add(new ResourceLink<>("downloadLink", download));
	}

	protected ICriteriaBuilder makeCriteriaBuilder() {
		EventCriteriaBuilder eventCriteriaBuilder = new EventCriteriaBuilder();
		SingleSortState<String> defaultSort = new SingleSortState<String>();
		defaultSort.setSort(new SortParam<>("startTime", false)); // Sort by Insert Time by default
		eventCriteriaBuilder.setSortState(defaultSort);
		return eventCriteriaBuilder;
	}

	protected SortableHibernateProvider<Event> makeHibernateProvider(ICriteriaBuilder builder) {
		SortableHibernateProvider<Event> provider = new SortableHibernateProvider<>(Event.class, builder);
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
		List<? extends IEventType> allEventTypes = eventService.listEventTypes();
		List<IEventType> eventTypes = new ArrayList<>();
		eventTypes.addAll(allEventTypes);
		numberOfEventTypes = eventTypes.size();
		showEventTypesM = new ListModel<IEventType>(eventTypes);
		form.add(new CheckBoxMultipleChoice<IEventType>("type", showEventTypesM, allEventTypes,
				new ChoiceRenderer<IEventType>("displayName", "name"))
				.setSuffix("<br/>"));
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
		numberOfSites = sites.size();
		showSitesM = new ListModel<>(sites);
		if (!allSites.getObject().isEmpty())
			form.add(new CheckBoxMultipleChoice<>("site", showSitesM, allSites,
					new ChoiceRenderer<Site>("name", "id")));
		else
			form.add(new WebMarkupContainer("site").setVisible(false));
	}
	
	protected void addOtherFilters(Form<Object> form) {
		inAPeriod = new Model<Boolean>(false);
		form.add(new CheckBox("showNoSite", inAPeriod));		

		showPermissionUsers = new Model<Boolean>(true);
		form.add(new CheckBox("showPermissionUsers", showPermissionUsers));
	}

	protected List<IDataColumn<Event>> makeColumns() {
		List<IDataColumn<Event>> columns = new ArrayList<IDataColumn<Event>>();
		
		columns.add(new PropertyDataColumn<Event>("EventID", "id", "id"));

		columns.add(new DateDataColumn<Event>("Start time", "startTime"));
		columns.add(new DateDataColumn<Event>("End time", "endTime"));
		
		columns.add(new PropertyDataColumn<Event>("User", "user.subjectId", "user.subjectId"));
		columns.add(new PropertyDataColumn<Event>("Event Type", "type", "type.displayName"));
		columns.add(new PropertyDataColumn<Event>("Details", "detail"));
		columns.add(new PropertyDataColumn<Event>("Page", "page"));

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

	public class EventCriteriaBuilder implements ICriteriaBuilder, ISortStateLocator<String> {

		@Getter @Setter 
		private ISortState<String> sortState;
		
		@Override
		public void buildUnordered(Criteria criteria) {
			
			// Type check
			if (showEventTypesM.getObject().size() < numberOfEventTypes)
				criteria.add(Restrictions.in("type", showEventTypesM.getObject()));
			else
				log.debug("Not filtering by event type");

			criteria.createAlias("user", "user");
			
			// Site Check
			List<Site> siteList = showSitesM.getObject();
			if (siteList.size() < numberOfSites || inAPeriod.getObject()) {
				criteria.createAlias("user.periods", "period", JoinType.LEFT_OUTER_JOIN);
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
			} else {
				log.debug("Not filtering by period/site");
			}
			
			if (fromDateM.getObject()!=null && toDateM.getObject() != null) {
				Date startDate = midnightStart(fromDateM.getObject());
				Date endDate = midnightEnd(toDateM.getObject());
				log.debug("Considering events between {} and {}", startDate, endDate);
				criteria.add(Restrictions.between("startTime", startDate, endDate));
			}

			//set permission check
			if (showPermissionUsers.getObject()) {
				criteria.add(Restrictions.eq("user.permission", true));
			}
			
			// Also load ResponseData elements in the same query, to avoid thousands of subsequent queries.
			criteria.setFetchMode("responseData", FetchMode.JOIN);

			// The join with periods results in multiple rows for multi-period users.
			// Unfortunately, this confuses the dataprovider, which still counts the duplicates
			// and therefore doesn't return a full page full of items each time.
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);  // Remove duplicate rows as a result of the INNER JOIN
		}

		@Override
		public void buildOrdered(Criteria criteria) {
			buildUnordered(criteria);
			SortParam<String> sort = ((SingleSortState<String>) getSortState()).getSort();
			if (sort != null) {
				if (sort.isAscending())
					criteria.addOrder(Order.asc(sort.getProperty()).ignoreCase());
				else
					criteria.addOrder(Order.desc(sort.getProperty()).ignoreCase());
			}
		}
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		if (showSitesM != null)
			showSitesM.detach();
	}

}
