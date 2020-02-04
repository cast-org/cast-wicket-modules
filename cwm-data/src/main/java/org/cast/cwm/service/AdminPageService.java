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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.admin.*;
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
public class AdminPageService implements IAdminPageService {
	@Override
	public Class<? extends WebPage> getAdminHomePage() {
		return AdminHome.class;
	}

	@Override
	public Class<? extends WebPage> getSiteListPage() {
		return SiteListPage.class;
	}

	@Override
	public Class<? extends WebPage> getSiteEditPage() {
		return SiteInfoPage.class;
	}

	@Override
	public Class<? extends WebPage> getPeriodEditPage() {
		return PeriodInfoPage.class;
	}

	@Override
	public Class<? extends WebPage> getUserListPage() {
		return UserListPage.class;
	}

	@Override
	public Class<? extends WebPage> getUserEditPage() {
		return UserFormPage.class;
	}

	@Override
	public Class<? extends WebPage> getBulkUpdatePage() {
		return BulkUpdatePage.class;
	}

	@Override
	public ISpreadsheetReader getUserSpreadsheetReader() {
		return new UserSpreadsheetReader();
	}

	@Override
	public ISpreadsheetReader getUserUpdateSpreadsheetReader() {
		return new UserUpdateSpreadsheetReader();
	}

	@Override
	public Link getSiteListPageLink(String wicketId) {
		return new BookmarkablePageLink(wicketId, getSiteListPage());
	}

	public Link getSiteEditPageLink(String wicketId, IModel<? extends Site> mSite) {
		BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(wicketId, getSiteEditPage());
		link.getPageParameters().set("siteId", mSite.getObject().getId());
		return link;
	}

	@Override
	public Link getNewSiteEditPageLink(String wicketId) {
		return new BookmarkablePageLink<Void>(wicketId, getSiteEditPage());
	}

	@Override
	public Link getPeriodEditPageLink(String wicketId, IModel<? extends Period> mPeriod) {
		return new BookmarkablePageLink<Void>(wicketId, getPeriodEditPage(),
				new PageParameters().set("periodId", mPeriod.getObject().getId()));
	}

	@Override
	public Link getNewPeriodEditPageLink(String wicketId, IModel<? extends Site> mSite) {
		return new BookmarkablePageLink<Void>(wicketId, getPeriodEditPage(),
				new PageParameters().set("siteId", mSite.getObject().getId()));
	}

	@Override
	public Link getUserListPageLink(String wicketId) {
		return new BookmarkablePageLink<Void>("link", getUserListPage());
	}

	@Override
	public Link getUserEditPageLink(String wicketId, IModel<? extends User> mUser) {
		return new BookmarkablePageLink<Void>(wicketId, getUserEditPage(),
				new PageParameters().set("userId", mUser.getObject().getId()));
	}

	@Override
	public Link getNewUserEditPageLink(String wicketId, Role role, IModel<? extends Period> mPeriod) {
		PageParameters pp = new PageParameters().set("role", role.name());
		if (mPeriod != null && mPeriod.getObject() != null)
			pp.set("periodId", mPeriod.getObject().getId());
		return new BookmarkablePageLink<Void>(wicketId, getUserEditPage(), pp);
	}

}
