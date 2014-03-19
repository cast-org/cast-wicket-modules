package org.cast.cwm.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.file.Path;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class CwmTestApplication<T> extends MockApplication {

	protected Map<Class<T>, T> injectionMap;

	public CwmTestApplication() {
		this(new HashMap<Class<T>, T>());
	}
	
	public CwmTestApplication(Map<Class<T>, T> injectionMap) {
		this.injectionMap = injectionMap;
	}


	@Override
	public void init() {
		super.init();
		getComponentInstantiationListeners().add(new GuiceComponentInjector(this, getGuiceInjector()));
	    // Check separate "theme" folder for markup and XSL styles.
	    getResourceSettings().getResourceFinders().add(new Path(getThemeDir()));		
	}

	protected String getThemeDir() {
	    return "theme";
	}

	protected Injector getGuiceInjector() {
		return Guice.createInjector(new AbstractModule(){
			@Override
			protected void configure() {
				for (Class<T> keyClass: injectionMap.keySet()) {
					bind(keyClass).toInstance(injectionMap.get(keyClass));
				}
			}
		});
	}

}