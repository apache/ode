docker pull trade4chor/maven:2.2.1-jdk6

SET JAVA_OPTS=-Xmx1024M -XX:MaxPermSize=512M
SET MVN_ARGS=%*
SET PWD=%CD:\=/%

REM @hahnml: Use the current working directory for caching maven artifacts
docker run -it --rm -v %PWD%:/opt/opentosca/ode -v %PWD%/.m2:/root/.m2 -w /opt/opentosca/ode --entrypoint mvn trade4chor/maven:2.2.1-jdk6 %MVN_ARGS%
