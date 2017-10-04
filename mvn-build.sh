#!/bin/sh

docker pull trade4chor/maven:2.2.1-jdk6

export JAVA_OPTS="-Xmx1024M -XX:MaxPermSize=512M"
MVN_ARGS="$@"
CONTAINER_USERNAME="dummy"
CONTAINER_GROUPNAME="dummy"
HOMEDIR="/home/$CONTAINER_USERNAME"
GROUP_ID=$(id -g)
USER_ID=$( id -u)

CREATE_USER_COMMAND="groupadd -f -g $GROUP_ID $CONTAINER_GROUPNAME \
&& useradd -u $USER_ID -g $CONTAINER_GROUPNAME $CONTAINER_USERNAME \
&& mkdir --parent $HOMEDIR \
&& chown -R $CONTAINER_USERNAME:$CONTAINER_GROUPNAME $HOMEDIR"

MVN_COMMAND="su $CONTAINER_USERNAME -c 'mvn $MVN_ARGS'"

FINAL_COMMAND="$CREATE_USER_COMMAND && $MVN_COMMAND"

docker run --rm \
    -e JAVA_OPTS \
    -v `pwd`:/opt/opentosca/ode \
    -v `pwd`/.m2:$HOMEDIR/.m2 \
    -w /opt/opentosca/ode \
    --entrypoint bash \
    trade4chor/maven:2.2.1-jdk6 -c "$FINAL_COMMAND";
