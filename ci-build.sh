# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#!/bin/sh

docker pull vanto/apache-buildr:latest-jruby-jdk7

export JAVA_OPTS="-Xmx1024M -XX:MaxPermSize=512M"
BUILDR_ARGS="$@"
CONTAINER_USERNAME="dummy"
CONTAINER_GROUPNAME="dummy"
HOMEDIR="/home/$CONTAINER_USERNAME"
GROUP_ID=$(id -g)
USER_ID=$( id -u)

CREATE_USER_COMMAND="groupadd -f -g $GROUP_ID $CONTAINER_GROUPNAME && useradd -u $USER_ID -g $CONTAINER_GROUPNAME $CONTAINER_USERNAME && mkdir --parent $HOMEDIR && chown -R $CONTAINER_USERNAME:$CONTAINER_GROUPNAME $HOMEDIR"

BUNDLER_COMMAND="/opt/jruby/bin/jruby -S bundler install --gemfile=/workspace/Gemfile"
 
BUILDR_COMMAND="su $CONTAINER_USERNAME -c '/opt/jruby/bin/jruby -S buildr $BUILDR_ARGS'"

FINAL_COMMAND="$CREATE_USER_COMMAND && $BUNDLER_COMMAND && $BUILDR_COMMAND"

docker run --rm -t -i -e JAVADOC=no -e JAVA_OPTS -v `pwd`:/workspace -v $HOME/.m2:/home/dummy/.m2  -v /tmp:/tmp --entrypoint bash vanto/apache-buildr:latest-jruby-jdk7 -c "$FINAL_COMMAND"