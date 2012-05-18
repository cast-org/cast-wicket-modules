package org.cast.cwm;

import java.util.List;
import java.util.Properties;

import net.databinder.hib.Databinder;
import net.databinder.hib.SessionUnit;

import org.apache.wicket.injection.web.InjectorHolder;
import org.cast.cwm.data.init.IDatabaseInitializer;
import org.cast.cwm.service.CwmService;
import org.cast.cwm.service.ICwmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

class DatabaseInitializerRunner {

	private static final Logger log = LoggerFactory.getLogger(CwmApplication.class);

	private Properties appProperties;

	@Inject
	private ICwmService cwmService;

	DatabaseInitializerRunner(Properties appProperties) {
		this.appProperties = appProperties;
		InjectorHolder.getInjector().inject(this);
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
