package com.expedia.www.haystack.examples.dropwizard.resources;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.expedia.haystack.annotations.DisableTracing;

@DisableTracing
@Path("/do-not-trace-me")
@Produces(MediaType.APPLICATION_JSON)
public class UntracedResource extends HelloWorldResource {

    public UntracedResource(String template, String defaultName) {
        super(template, defaultName);
    }
}
