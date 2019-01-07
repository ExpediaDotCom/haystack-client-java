#!/bin/bash
cd `dirname $0`/..

if [ "${TRAVIS_BRANCH}" == 'master' -a "${TRAVIS_PULL_REQUEST}" == 'false' ] || [ -n "${TRAVIS_TAG}" ]; then
  if [[ -z "${SONATYPE_USERNAME}" || -z "${SONATYPE_PASSWORD}" ]]; then
    echo "ERROR! Please set SONATYPE_USERNAME and SONATYPE_PASSWORD environment variable"
    exit 1
  fi

  if [ ! -z "${TRAVIS_TAG}" ]; then
    echo "travis tag is set -> updating pom.xml <version> attribute to ${TRAVIS_TAG}"
    ./mvnw --batch-mode --settings .travis/settings.xml -DskipTests=true -DreleaseVersion=${TRAVIS_TAG} release:clean release:prepare release:perform
    SUCCESS=$?
  else
    echo "no travis tag is set, hence keeping the snapshot version in pom.xml"
    ./mvnw --batch-mode --settings .travis/settings.xml clean deploy -DskipTests=true -B -U
    SUCCESS=$?
  fi

  if [ ${SUCCESS} -eq 0 ]; then
    echo "successfully deployed the jars to nexus"
  fi

  exit ${SUCCESS}
else
  echo "Skipping artifact deployment for branch ${TRAVIS_BRANCH} with PR=${TRAVIS_PULL_REQUEST} and TAG=${TRAVIS_TAG}"
fi

exit 0
