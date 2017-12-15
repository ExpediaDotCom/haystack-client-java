JAX-RS 2
========
This provides generic [JAX-RS 2][jaxrs2] instrumentation.  It only uses opentracing interfaces and should be portable to other compliant implementations.


# Usage #
This provides instrumentation that adheres to the JAX-RS 2.0 specification.  It abstains from using any specific implementation, Tracing nor JAX-RS, and as such should be portable to any specific implementation.  It provides only a set of filters, for the server and client, to create tracing around the request/response lifecycle.


## See Also ##
See other modules in this project for additional instrumentation for a specific implementations; [Jersey][jersey], [Dropwizard][dropwizard] and others.

[jaxrs2]: https://jax-rs.github.io/apidocs/2.1/
[jersey]: https://jersey.java.net
[dropwizard]: http://www.dropwizard.io
