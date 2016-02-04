#!/bin/bash

mvn -B -DrepoToken="${coverallsToken}" jacoco:report coveralls:report
