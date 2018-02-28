package com.expedia.www.haystack.examples.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.expedia.haystack.jaxrs2.feature.HaystackFeature;
import com.expedia.www.haystack.examples.dropwizard.health.TemplateHealthCheck;
import com.expedia.www.haystack.examples.dropwizard.resources.HelloWorldResource;
import com.expedia.www.haystack.examples.dropwizard.resources.UntracedResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

    public static void main(final String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    private static class BridgeDropwizardMeterRegistry extends DropwizardMeterRegistry {
        public BridgeDropwizardMeterRegistry(DropwizardConfig config, MetricRegistry registry) {
            super(config, registry, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM);
            this.config().namingConvention(NamingConvention.dot);
        }

        @Override
        protected Double nullGaugeValue() {
            return Double.NaN;
        }
    }

    private static interface SimpleDropwizardConfig extends DropwizardConfig {
        SimpleDropwizardConfig DEFAULT = k -> null;

        @Override
        default String prefix() {
            return "hello-world";
        }
    }

    @Override
    public void initialize(final Bootstrap<HelloWorldConfiguration> bootstrap) {
    }

    @Override
    public void run(final HelloWorldConfiguration configuration,
                    final Environment environment) {
        // register all haystack-client metrics into the built-in registry
        Metrics.addRegistry(new BridgeDropwizardMeterRegistry(SimpleDropwizardConfig.DEFAULT, environment.metrics()));

        final HaystackFeature haystackFeature = new HaystackFeature(configuration.getTracer().build());
        environment.jersey().register(haystackFeature);

        final HelloWorldResource resource = new HelloWorldResource(configuration.getTemplate(),
                                                                   configuration.getDefaultName());

        environment.jersey().register(resource);

        environment.jersey().register(new UntracedResource(configuration.getTemplate(),
                                                           configuration.getDefaultName()));

		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		environment.healthChecks().register("template", healthCheck);
	}

}
