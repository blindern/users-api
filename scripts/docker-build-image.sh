#!/bin/sh
set -eu

docker --version

repo="blindern/users-api"
tag=$(date -u +%Y%m%d-%H%M)-$CIRCLE_BUILD_NUM
echo $repo >.dockerrepo
echo $tag >.dockertag

# Pull latest image and use as cache
docker pull $repo:latest || :

docker build --pull --cache-from $repo:latest -t $repo:$tag .
