[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-client-java.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-client-java)
[![codecov](https://codecov.io/gh/ExpediaDotCom/haystack-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/ExpediaDotCom/haystack-client-java)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

# haystack-client-java
Haystack bindings for the OpenTracing API. Clients use this library to send trace data to a Haystack server.


## How to build the code

Since this repository contains `haystack-idl` as a submodule, you must use the following command line to clone the repository:
```
git clone --recursive git@github.com:ExpediaDotCom/haystack-client-java.git .
```

### Prerequisites

* Make sure you have Java 1.8

### Building

For a full build, including unit tests you can run the following
```
./mvnw clean package
```
