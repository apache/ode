#!/bin/sh

docker pull trade4chor/maven:2.2.1-jdk6

export JAVA_OPTS="-Xmx1024M -XX:MaxPermSize=512M"
MVN_ARGS="$@"

docker run --rm \
    -e JAVA_OPTS \
    -v `pwd`:/opt/opentosca/ode \
    -v `pwd`/.m2:/root/.m2 \
    -w /opt/opentosca/ode \
    --entrypoint mvn \
    trade4chor/maven:2.2.1-jdk6 $MVN_ARGS;
