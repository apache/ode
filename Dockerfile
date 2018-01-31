FROM sathwik/apache-buildr:latest-jruby-jdk8 as builder

ENV JAVA_OPTS="-Xmx1024M -XX:MaxPermSize=512M" BUILDR_ARGS="-f Rakefile clean package test=no JAVADOC=off"

RUN mkdir /build

COPY . /workspace

RUN jruby -S bundler install --gemfile=/workspace/Gemfile \
    && jruby -S bundle update diff-lcs \
    && buildr $BUILDR_ARGS && cp /workspace/axis2-war/target/ode-axis2-war-1.4-SNAPSHOT.war /build/ode.war

FROM tomcat:8.5-jre8

LABEL maintainer "Michael Hahn <mhahn.dev@gmail.com>"

RUN apt-get update -qq && apt-get install -qqy unzip

COPY --from=builder /build/ode.war ${CATALINA_HOME}/webapps
RUN unzip ${CATALINA_HOME}/webapps/ode.war -d ${CATALINA_HOME}/webapps/ode

EXPOSE 8080

CMD ${CATALINA_HOME}/bin/catalina.sh run

#
# Manually build by running:
#
#   docker build -t hahnml/ode .
# 
# Start a new ODE container by running:
# 
#   docker run --name ode -d -p 8080:8080 hahnml/ode
# 
# The web UI of Apache ODE is available under: http://localhost:8080/ode/#/
# 
# To stop and remove the created container run:
# 
#   docker stop ode && docker rm ode
#
# Process bundles can be deployed via the web UI [http://localhost:8080/ode/#/processes].
# 
# Alternatively, you can mount a directory containing process bundles (folders containing process files) of the host into the container as follows:
# 
#   docker run --name ode -d -p 8080:8080 --mount type=bind,source=[path/to/folder/on/host]/,target=/usr/local/tomcat/webapps/ode/WEB-INF/processes hahnml/ode
# 
