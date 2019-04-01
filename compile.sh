#!/bin/bash

set -euf -o pipefail

BUILD_PROPERTIES_FILENAME=$1
VISION_PARSER_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

printf "Recompiling library\n"

(cd $VISION_PARSER_DIR/lib/spf && \
	ant -propertyfile build.properties dist \
	&& ant -propertyfile $BUILD_PROPERTIES_FILENAME dist)
