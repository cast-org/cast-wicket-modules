/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.service;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;

/**
 * Methods to build links to administrative pages.
 * This allows applications to selectively choose to replace admin pages with enhanced versions.
 *
 * @author bgoldowsky
 */
public interface IAdminPageService {

	Class<? extends WebPage> getAdminHomePage();

	Class<? extends WebPage> getSiteListPage();

	Class<? extends WebPage> getSiteEditPage();

	Class<? extends WebPage> getPeriodEditPage();

	Class<? extends WebPage> getUserListPage();

	Class<? extends WebPage> getUserEditPage();

	Class<? extends WebPage> getBulkUpdatePage();

	ISpreadsheetReader getUserSpreadsheetReader();

	ISpreadsheetReader getUserUpdateSpreadsheetReader();

	/**
	 * Return a link to the page listing all sites and periods.
	 *
	 * @param wicketId wicket ID of the component
	 * @return link component
	 */
	Link getSiteListPageLink(String wicketId);

	/**
	 * Return a link to the page for editing a {@link Site}.
	 *
	 * @param wicketId wicket ID of the component
	 * @param mSite model of site to edit
	 * @return link component
	 */
	Link getSiteEditPageLink(String wicketId, IModel<? extends Site> mSite);

	/**
	 * Return a link to a page that creates and edits a new {@link Site}.
	 *
	 * @param wicketId wicket ID of the component
	 * @return link component
	 */
	Link getNewSiteEditPageLink(String wicketId);

	/**
	 * Return a link to the page for editing a {@link Period}.
	 *
	 * @param wicketId wicket ID of the component
	 * @param mPeriod model of period to edit
	 * @return link component
	 */
	Link getPeriodEditPageLink(String wicketId, IModel<? extends Period> mPeriod);

	/**
	 * Return a link to a page that creates and edits a new Period in the given Site.
	 *
	 * @param wicketId wicket ID of the component
	 * @param mSite model of site within which the new Period will be created
	 * @return link component
	 */
	Link getNewPeriodEditPageLink(String wicketId, IModel<? extends Site> mSite);

	/**
	 * Return a link to a page that lists users.
	 *
	 * @param wicketId wicket ID of the component
	 * @return link component
	 */
	Link getUserListPageLink(String wicketId);

	/**
	 * Return a link to the page for editing a {@link User}.
	 *
	 * @param wicketId wicket ID of the component
	 * @param mUser model of user to edit
	 * @return link component
	 */
	Link getUserEditPageLink(String wicketId, IModel<? extends User> mUser);

	/**
	 * Return a link to the page for creating a new {@link User}.
	 * @param wicketId wicket id of the component
	 * @param role type of User to create
	 * @param mPeriod optional, period that new user should be in.
	 * @return link component
	 */
	Link getNewUserEditPageLink(String wicketId, Role role, IModel<? extends Period> mPeriod);

}
