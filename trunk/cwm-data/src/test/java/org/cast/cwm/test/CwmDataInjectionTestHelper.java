package org.cast.cwm.test;

import static org.mockito.Mockito.when;

import org.apache.wicket.model.Model;
import org.cast.cwm.service.ICwmSessionService;

public class CwmDataInjectionTestHelper extends InjectionTestHelper {

	public CwmDataInjectionTestHelper() {
		super();
	}

	public ICwmSessionService injectAndStubCwmSessionService (CwmDataBaseTestCase baseInjectedTestCase) {
		ICwmSessionService cwmSessionService = injectMock(ICwmSessionService.class);
	    when(cwmSessionService.isSignedIn()).thenReturn(true);
	    when(cwmSessionService.getUserModel()).thenReturn(Model.of(baseInjectedTestCase.loggedInUser));
	    when(cwmSessionService.getUser()).thenReturn(baseInjectedTestCase.loggedInUser);
	    when(cwmSessionService.getCurrentPeriodModel()).thenReturn(Model.of(baseInjectedTestCase.period));
		return cwmSessionService;
	}

}
