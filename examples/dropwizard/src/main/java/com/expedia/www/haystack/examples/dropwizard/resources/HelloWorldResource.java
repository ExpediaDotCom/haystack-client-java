package com.expedia.www.haystack.examples.dropwizard.resources;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.expedia.haystack.annotations.DisableTracing;
import com.expedia.haystack.annotations.Traced;
import com.expedia.www.haystack.examples.dropwizard.api.Saying;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;

    public HelloWorldResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(template, name.orElse(defaultName));
        return new Saying(counter.incrementAndGet(), value);
    }

    @GET
    @Timed
    @Path("/ignored")
    @DisableTracing
    public Saying sayHelloNotTracked(@QueryParam("name") Optional<String> name) {
        return sayHello(name);
    }

    @GET
    @Timed
    @Path("/dolly")
    @Traced(name="hello-dolly")
    public Saying sayHelloDolly(@QueryParam("name") Optional<String> name) {
        return sayHello(name);
    }

}