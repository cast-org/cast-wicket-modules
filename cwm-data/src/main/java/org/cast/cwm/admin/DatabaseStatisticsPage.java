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

import net.databinder.hib.Databinder;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.stat.Statistics;

@AuthorizeInstantiation("ADMIN")
public class DatabaseStatisticsPage extends AdminPage {

	private static final long serialVersionUID = 1L;

	public DatabaseStatisticsPage (PageParameters params) {
		super(params);
		
		Statistics stats = Databinder.getHibernateSessionFactory().getStatistics();
		if (stats.isStatisticsEnabled()) {
			add(new Label("count", String.valueOf(stats.getQueryExecutionCount())));
			add(new Label("cloads", String.valueOf(stats.getCollectionLoadCount())));
			add(new Label("worst", stats.getQueryExecutionMaxTimeQueryString())); // Worked in SNUDLE, why am I getting no output here?
			add(new Label("worsttime", String.valueOf(stats.getQueryExecutionMaxTime())));
			add(new Label("qchit", String.valueOf(stats.getQueryCacheHitCount())));
			add(new Label("qcmiss", String.valueOf(stats.getQueryCacheMissCount())));
		} else {
			stats.setStatisticsEnabled(true);
			add(new Label("count",     "--"));
			add(new Label("cloads",    "--"));
			add(new Label("worst",     "--"));
			add(new Label("worsttime", "--"));
			add(new Label("qchit",     "--"));
			add(new Label("qcmiss",    "--"));
		}
		stats.clear();
		
		add (new Link<Void>("off") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				Databinder.getHibernateSessionFactory().getStatistics().setStatisticsEnabled(false);
				setResponsePage(getApplication().getHomePage());
			}
			
		});
	}
}
