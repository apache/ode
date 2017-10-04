docker pull trade4chor/maven:2.2.1-jdk6

SET JAVA_OPTS=-Xmx1024M -XX:MaxPermSize=512M
SET MVN_ARGS=%*
SET CONTAINER_USERNAME=dummy
SET CONTAINER_GROUPNAME=dummy
SET HOMEDIR=/home/%CONTAINER_USERNAME%
SET GROUP_ID=$(id -g)
SET USER_ID=$(id -u)

SET PWD=%CD:\=/%

SET CREATE_USER_COMMAND=groupadd -f -g %GROUP_ID% %CONTAINER_GROUPNAME% ^&^& useradd -o -u %USER_ID% -g %CONTAINER_GROUPNAME% %CONTAINER_USERNAME% ^&^& mkdir --parent %HOMEDIR% ^&^& chown -R %CONTAINER_USERNAME%:%CONTAINER_GROUPNAME% %HOMEDIR% 

SET MVN_COMMAND=su %CONTAINER_USERNAME% -c 'mvn %MVN_ARGS%'

REM @hahnml: Use the current working directory for caching maven artifacts
docker run -it --rm -v %PWD%:/opt/opentosca/ode -v %PWD%/.m2:/home/dummy/.m2 -w /opt/opentosca/ode --entrypoint bash trade4chor/maven:2.2.1-jdk6 -c "%CREATE_USER_COMMAND% && %MVN_COMMAND%"
