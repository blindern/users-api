#!/bin/sh
set -eu

repo=$(cat .dockerrepo)
tag=$(cat .dockertag)
image_id="$repo:$tag"

exec docker run --rm -p 8000:8000 "$image_id"
