package com.expedia.www.haystack.examples.dropwizard;

import com.expedia.haystack.jaxrs2.feature.HaystackFeature;
import com.expedia.www.haystack.examples.dropwizard.resources.HelloWorldResource;
import com.expedia.www.haystack.examples.dropwizard.health.TemplateHealthCheck;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

    public static void main(final String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(final Bootstrap<HelloWorldConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final HelloWorldConfiguration configuration,
                    final Environment environment) {
        final HaystackFeature haystackFeature = new HaystackFeature(configuration.getTracer().build());
        environment.jersey().register(haystackFeature);

        final HelloWorldResource resource = new HelloWorldResource(configuration.getTemplate(),
                                                                   configuration.getDefaultName());

        environment.jersey().register(resource);

        final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
    }

}
