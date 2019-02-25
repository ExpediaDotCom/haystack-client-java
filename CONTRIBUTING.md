# Contributing

Code contributions are always welcome! 

* Open an issue in the repo with defect/enhancements
* We can also be reached @ https://gitter.im/expedia-haystack/Lobby
* Fork, make the changes, build and test it locally
* Issue a PR - watch the PR build in [travis-ci](https://travis-ci.org/ExpediaDotCom/haystack-client-java)
* Once merged to master, travis-ci will build and release the artifact with the current snapshot version


## Building the code

### Prerequisites

* Make sure you have Java 1.8

### Building

For a full build, including unit tests you can run the following

```
./mvnw clean package
```

## Releasing the current changes

Latest releases of this library are available in [Maven central](https://mvnrepository.com/search?q=Haystack&d=com.expedia)

1. Get All your changes reviewed and merged into master
2. Test the latest snapshot build in [SonaType Snapshots Repository](https://oss.sonatype.org/#nexus-search;quick~haystack-client-java) to ensure the artifact contains all your changes and they work as expected
3. Once merged to master, one can use github https://github.com/ExpediaDotCom/haystack-client-java/releases or manual tagging to the next semantic version
4. This will cause the build to run for the new tagged version. This will cause the jar files to be released to the 
[SonaType Release Repository](https://oss.sonatype.org/#nexus-search;quick~haystack-client-java).
5. Now, update the POM version to next snapshot version for development
6. Create another pull request with the change from step 5 and get it merged.


