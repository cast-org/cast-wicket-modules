package org.cast.cwm.service;

import org.apache.wicket.injection.Injector;

import com.google.inject.Inject;
import com.google.inject.Provider;

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
