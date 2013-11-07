/*
 * Copyright 2011-2013 CAST, Inc.
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
package org.cast.cwm;

import java.util.List;

import net.databinder.hib.Databinder;
import net.databinder.hib.SessionUnit;

import org.apache.wicket.injection.Injector;
import org.cast.cwm.data.init.IDatabaseInitializer;
import org.cast.cwm.service.ICwmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

class DatabaseInitializerRunner {

	private static final Logger log = LoggerFactory.getLogger(CwmApplication.class);

	private IAppConfiguration appProperties;

	@Inject
	private ICwmService cwmService;

	DatabaseInitializerRunner(IAppConfiguration configuration) {
		this.appProperties = configuration;
		Injector.get().inject(this);
	}
	
	void run(final List<IDatabaseInitializer> databaseInitializers) {
		Databinder.ensureSession(new SessionUnit() {

			public Object run(org.hibernate.Session session) {
				List<String> initsDone = cwmService.getInitializationNames();
				for (IDatabaseInitializer init : databaseInitializers) {
					// Run the initializer unless it's a one-time-only that's already done.
					if (!init.isOneTimeOnly() || !initsDone.contains(init.getName())) {
						log.debug("Running {}", init.getName());
						if (init.run(appProperties)) {
							// record the run if it did any work.
							cwmService.saveInitialization(init);
						}
					}
				}
				return null;
			}
		});
	}
}
