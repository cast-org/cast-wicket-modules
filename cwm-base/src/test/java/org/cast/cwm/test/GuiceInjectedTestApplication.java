package org.cast.cwm.test;

import java.util.Map;

import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.util.tester.WicketTester.DummyWebApplication;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceInjectedTestApplication<T> extends DummyWebApplication {
	
	Map<Class<T>, T> injectionMap;
	
	public GuiceInjectedTestApplication(Map<Class<T>, T> injectionMap) {
		this.injectionMap = injectionMap;
	}
	
	@Override
	public void init() {
		super.init();
		addComponentInstantiationListener(new GuiceComponentInjector(this, getGuiceInjector()));
	}

	protected Injector getGuiceInjector()
	{
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
