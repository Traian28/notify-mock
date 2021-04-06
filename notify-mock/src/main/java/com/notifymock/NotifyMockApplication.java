package com.notifymock;

import com.notifymock.core.MockWorker;
import com.notifymock.guice.ApplicationModule;
import com.eadp.guice.DogstatsdModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class NotifyMockApplication extends Application<NotifyMockConfiguration> {

	public static void main(final String[] args) throws Exception {
		new NotifyMockApplication().run(args);
	}

	@Override
	public String getName() {
		return "NotifyMock";
	}

	@Override
	public void initialize(final Bootstrap<NotifyMockConfiguration> bootstrap) {
		// TODO: application initialization
	}

	@Override
	public void run(final NotifyMockConfiguration configuration,
					final Environment environment) {
		final Injector injector = Guice.createInjector(new ApplicationModule(),
			new DogstatsdModule("notification-worker", "load"));
		environment.lifecycle().manage(injector.getInstance(MockWorker.class));
	}

}
