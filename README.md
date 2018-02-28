[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-client-java.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-client-java)
[![codecov](https://codecov.io/gh/ExpediaDotCom/haystack-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/ExpediaDotCom/haystack-client-java)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

# haystack-client-java
Haystack bindings for the OpenTracing API


## How to build the code?

Since this repo contains haystack-idl as the submodule, so use the following to clone the repo
* git clone --recursive git@github.com:ExpediaDotCom/haystack-agent.git .

### Prerequisites

* Make sure you have Java 1.8

### Building

For a full build, including unit tests you can run the following
```
./mvnw clean package
```
