package com.notifymock.guice;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {

		final Config config = ConfigFactory.load();

		bind(Config.class).toInstance(config);



	}

}
