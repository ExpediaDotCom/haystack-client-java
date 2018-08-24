[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-client-java.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-client-java)
[![codecov](https://codecov.io/gh/ExpediaDotCom/haystack-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/ExpediaDotCom/haystack-client-java)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

# haystack-client-java
Haystack bindings for the OpenTracing API. Clients use this library to send trace data to a Haystack server.


## How to build the code

Since this repository contains `haystack-idl` as a submodule, you must use the following command line to clone the repository:
```
git clone git@github.com:ExpediaDotCom/haystack-client-java.git .
```

### Prerequisites

* Make sure you have Java 1.8

### Building

For a full build, including unit tests you can run the following
```
./mvnw clean package
```

### Releases
1. Decide what kind of version bump is necessary, based on [Semantic Versioning](http://semver.org/) conventions.
In the items below, the version number you select will be referred to as `x.y.z`.
2. Update the [pom.xml](https://github.com/ExpediaDotCom/haystack-client-java/blob/master/pom.xml),
changing the version element to `<version>x.y.z</version>`. Note the *removal* of the `-SNAPSHOT` suffix.
3. Update the
[ReleaseNotes.md]((https://github.com/ExpediaDotCom/haystack-client-java/blob/master/ReleaseNotes.md))
file with details of your changes.
5. Create a pull request with your changes.
6. Ask for a review of the pull request; when it is approved, the Travis CI build will upload the resulting jar file
to the [SonaType Staging Repository](https://oss.sonatype.org/#stagingRepositories).
This will cause the jar file to be released to the 
[SonaType Release Repository](https://oss.sonatype.org/#nexus-search;quick~haystack-client-java).
7. Now you have to *put back* the -SNAPSHOT that you removed in step 2. When you do that, bump the `z` value to `z + 1`
to minimize the chance of somebody trying to release on top of the x.y.z version you just released. Note that the next
release might not use `x.y.z+1`; it could instead be `x.y+1.0` or `x+1.0.0`
8. Create another pull request with the change from step 7 and get it merged.