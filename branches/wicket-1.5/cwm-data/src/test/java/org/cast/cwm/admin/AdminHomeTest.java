package org.cast.cwm.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.IAppConfiguration;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.junit.Before;
import org.junit.Test;

public class AdminHomeTest {

	private WicketTester tester;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
        Map<Class<? extends Object>, Object> injectionMap = new HashMap<Class<? extends Object>, Object>();
        
        IAppConfiguration configuration = mock(IAppConfiguration.class);
		// when(configuration.getById(BinaryFileData.class, 1L)).thenReturn(mSampleBFD);
        injectionMap.put(IAppConfiguration.class, configuration);
        
        ICwmSessionService cwmSessionService = mock(ICwmSessionService.class);
        when(cwmSessionService.getUser()).thenReturn(new User(Role.ADMIN));
        injectionMap.put(ICwmSessionService.class, cwmSessionService);
        
		@SuppressWarnings("rawtypes")
		GuiceInjectedTestApplication application = new GuiceInjectedTestApplication(injectionMap);

		tester = new WicketTester(application);
	}
	
	@Test
	public void canRender() {
		tester.startPage(AdminHome.class);
		tester.assertRenderedPage(AdminHome.class);
		tester.assertContains("Administrator area");
		tester.assertBookmarkablePageLink("linkRepeater:1:link", UserListPage.class, new PageParameters());
	}
	
}
