#!/bin/bash
cd `dirname $0`/..

BRANCH=${TRAVIS_BRANCH}
PULL_REQUEST=${TRAVIS_PULL_REQUEST}
USERNAME=${SONATYPE_USERNAME}
PASSWORD=${SONATYPE_PASSWORD}
SHA=${TRAVIS_COMMIT}

# Only if this is a master branch and it is not a PR - meaning, this commit
# is from master branch most merge
if [[ "${BRANCH}" == 'master' && "${PULL_REQUEST}" == 'false' ]]; then
  if [[ -z "${USERNAME}" || -z "${PASSWORD}" ]]; then
    echo "ERROR! Please set SONATYPE_USERNAME and SONATYPE_PASSWORD environment variable"
    exit 1
  fi

  # if the current commit has a tag and if the tag matches the semantic versioning pattern `x.y.z`
  # then release it
  TAG_NAME=`git describe ${SHA} --tags`
  echo "Tag associated with the current commit ${SHA} is ${TAG_NAME}"

  if [[ ! -z "${TAG_NAME}" &&  ${TAG_NAME} =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    echo "travis tag is set -> updating pom.xml <version> attribute to ${TAG_NAME}"
    ./mvnw --batch-mode --settings .travis/settings.xml -DskipTests=true -DreleaseVersion=${TAG_NAME} release:clean release:prepare release:perform
    SUCCESS=$?
  else
    echo "no travis tag is set, hence keeping the snapshot version in pom.xml"
    ./mvnw --batch-mode --settings .travis/settings.xml clean deploy -DskipTests=true -B -U
    SUCCESS=$?
  fi

  if [[ ${SUCCESS} -eq 0 ]]; then
    echo "successfully deployed the jars to nexus"
  fi

  exit ${SUCCESS}
else
  echo "Skipping artifact deployment for branch ${BRANCH}, commit ${SHA} with PR=${PULL_REQUEST}"
  exit 0
fi
