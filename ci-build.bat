REM Licensed to the Apache Software Foundation (ASF) under one or more
REM contributor license agreements. See the NOTICE file distributed with
REM this work for additional information regarding copyright ownership.
REM The ASF licenses this file to You under the Apache License, Version 2.0
REM (the "License"); you may not use this file except in compliance with
REM the License. You may obtain a copy of the License at
REM
REM http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.

docker pull sathwik/apache-buildr:latest-jruby-jdk8

SET JAVA_OPTS=-Xmx1024M -XX:MaxPermSize=512M
SET BUILDR_ARGS=%*
SET CONTAINER_USERNAME=dummy
SET CONTAINER_GROUPNAME=dummy
SET HOMEDIR=/home/%CONTAINER_USERNAME%
SET GROUP_ID=$(id -g)
SET USER_ID=$(id -u)

SET PWD=%CD:\=/%

SET CREATE_USER_COMMAND=groupadd -f -g %GROUP_ID% %CONTAINER_GROUPNAME% ^&^& useradd -o -u %USER_ID% -g %CONTAINER_GROUPNAME% %CONTAINER_USERNAME% ^&^& mkdir --parent %HOMEDIR% ^&^& chown -R %CONTAINER_USERNAME%:%CONTAINER_GROUPNAME% %HOMEDIR% 

SET BUNDLER_COMMAND=jruby -S bundler install --gemfile=/workspace/Gemfile
 
SET BUILDR_COMMAND=su %CONTAINER_USERNAME% -c 'buildr %BUILDR_ARGS%'

REM For release set these arguments with proper values
REM  SET JAVADOC=on
REM  SET BUILDR_ENV=production
REM (Append -SNAPSHOT for ever next version)
REM  SET NEXT_VERSION=1.3.8-SNAPSHOT
REM  SET GNUPGHOME=%PWD%/.gnupg
REM  SET GPG_USER=
REM  SET GPG_PASS=

REM  mount volume for release
REM  -v %GNUPGHOME%:/home/dummy/.gnupg

REM @hahnml: Use the current working directory for caching maven and buildr artifacts
docker run --rm -e JAVADOC="%JAVADOC%" -e NEXT_VERSION="%NEXT_VERSION%" -e GPG_USER="%GPG_USER%" -e GPG_PASS="%GPG_PASS%" -e BUILDR_ENV="%BUILDR_ENV%" -e JAVA_OPTS="%JAVA_OPTS%" -v %PWD%:/workspace -v %PWD%/.m2:/home/dummy/.m2 -v %PWD%/.buildr:/home/dummy/.buildr -v %PWD%/tmp:/tmp --entrypoint bash sathwik/apache-buildr:latest-jruby-jdk8 -c "%CREATE_USER_COMMAND% && %BUNDLER_COMMAND% && %BUILDR_COMMAND%"
