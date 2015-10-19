#!/bin/bash

set -e

RELEASE_VERSION=$1

if [ -z "${RELEASE_VERSION}" ]; then
  echo 'Usage: ./release.sh $VERSION'
  exit 1
fi

TAG_NAME="v${RELEASE_VERSION}"

# Check for local modifications
HAS_CHANGES=`git status -s`
if [ "${HAS_CHANGES}" ]; then
  echo "Cannot release with local modfications:"
  echo "${HAS_CHANGES}"
  exit 1
fi

echo "Fetching Code"
git fetch origin master &> /dev/null

echo "Creating Local Release Branch"
git checkout origin/master &> /dev/null
git branch -D release &> /dev/null
git checkout -b release &> /dev/null

echo "Creating tag"
git tag -af "${TAG_NAME}" -m "${TAG_NAME}" origin/master &> /dev/null
git push --force origin "${TAG_NAME}" 

#./gradlew build upload
#./gradlew closeAndPromoteRepository