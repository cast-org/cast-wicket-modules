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

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.wicket.injection.Injector;

/**
 * Guice Provider that chooses one of the IUserPreferenceService implementations for the current session.
 * 
 * @author bgoldowsky
 *
 */
public class UserPreferenceServiceProvider implements Provider<IUserPreferenceService> {

	@Inject
	protected ICwmSessionService cwmSessionService;

	@Override
	public IUserPreferenceService get() {
		IUserPreferenceService service;
		if (cwmSessionService.getUser().isGuest())
			service = new GuestUserPreferenceService();
		else
			service = new UserPreferenceService();
		Injector.get().inject(service);
		return service;
	}

}
