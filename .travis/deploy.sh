#!/bin/bash
cd `dirname $0`/..

BRANCH=${TRAVIS_BRANCH}
PULL_REQUEST=${TRAVIS_PULL_REQUEST}
USERNAME=${SONATYPE_USERNAME}
PASSWORD=${SONATYPE_PASSWORD}
SHA=${TRAVIS_COMMIT}
TAG_NAME=${TRAVIS_TAG}


if [[ "${BRANCH}" == 'master' && "${PULL_REQUEST}" == 'false' ]] || [[ -n "${TAG_NAME}" ]]; then

  if [[ -z "${USERNAME}" || -z "${PASSWORD}" ]]; then
    echo "ERROR! Please set SONATYPE_USERNAME and SONATYPE_PASSWORD environment variable"
    exit 1
  fi
  if [[ ! -z "${TAG_NAME}" ]]; then
    echo "Ensuring that pom <version> matches ${TAG_NAME}"
    ./mvnw org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=${TAG_NAME}
  else
    echo "no travis tag is set, hence keeping the snapshot version in pom.xml"
  fi

  echo "Uploading to oss repo and GitHub"
  ./mvnw deploy --settings .travis/settings.xml -DskipTests=true --batch-mode --update-snapshots -Prelease
  SUCCESS=$?
  if [[ ${SUCCESS} -eq 0 ]]; then
    echo "successfully deployed the jars to nexus"
  fi

  exit ${SUCCESS}
else
  echo "Skipping artifact deployment for branch ${BRANCH} with PR=${PULL_REQUEST} and TAG=${TAG_NAME}"
fi

exit 0
