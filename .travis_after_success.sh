#!/bin/bash

mvn -B jacoco:report #coveralls:report

if [[ -n ${TRAVIS_TAG} ]]; then
    echo "Skipping deployment for tag \"${TRAVIS_TAG}\""
    exit
fi

if [[ ${TRAVIS_BRANCH} != 'master' ]]; then
    echo "Skipping deployment for branch \"${TRAVIS_BRANCH}\""
    exit
fi

if [[ "$TRAVIS_PULL_REQUEST" = "true" ]]; then
    echo "Skipping deployment for pull request"
    exit
fi

mvn clean release:clean release:prepare -B
mvn release:perform
