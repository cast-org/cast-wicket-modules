package org.cast.cwm.test;

import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.DummyHomePage;
import org.cast.cwm.test.GuiceInjectedTestApplication;

public class GuiceInjectedCwmTestApplication<T> extends GuiceInjectedTestApplication<T> {

	public GuiceInjectedCwmTestApplication(Map<Class<T>, T> injectionMap) {
		super(injectionMap);
	}
	
    @Override
    public void init() {
            super.init();
            // Check separate "theme" folder for markup and XSL styles.
            getResourceSettings().addResourceFolder(getThemeDir());
    }

    private String getThemeDir() {
            return "theme";
    }

    @Override
    public Class<? extends Page> getHomePage() {
            return DummyHomePage.class;
    }


}
