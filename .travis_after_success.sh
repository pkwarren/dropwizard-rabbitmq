#!/bin/bash

mvn -B jacoco:report coveralls:report

version_match=`perl -e "print '$TRAVIS_TAG' =~ /^dropwizard-rabbitmq-\d+\.\d+\.\d+\.\d+$/"`
if [[ "$version_match" == "1" ]]; then
    mvn versions:set -DnewVersion=$TRAVIS_TAG
    mvn clean deploy --settings release-settings.xml -DskipTests=true -B
fi
