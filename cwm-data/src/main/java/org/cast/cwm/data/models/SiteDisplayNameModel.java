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
package org.cast.cwm.data.models;

import com.google.inject.Inject;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.AbstractPropertyModel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;

/**
 * Model that displays an appropriate designator for a Site depending on the role of the logged-in User.
 *
 * Researchers and administrators should get anonymized views without actual site names displayed,
 * while the teachers and students should see the real name.
 */
public class SiteDisplayNameModel extends AbstractPropertyModel<String> {

	@Inject
	protected ICwmSessionService cwmSessionService;

	private static final long serialVersionUID = 1L;

	public SiteDisplayNameModel(IModel<Site> mSite) {
		super(mSite);
		Injector.get().inject(this);
	}

	@Override
	protected String propertyExpression() {
		User user = cwmSessionService.getUser();
		if (user == null)
			return "siteId";
		switch (user.getRole()) {
		case ADMIN:
		case RESEARCHER:
			return "siteId";
		case TEACHER:
		case STUDENT:
		case GUEST:
			return "name";
		default:
			throw new IllegalArgumentException("Unknown role: " + user);
		}
	}

}
