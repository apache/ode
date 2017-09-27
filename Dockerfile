FROM sathwik/apache-buildr:latest-jruby-jdk7 as builder

ENV JAVA_OPTS="-Xmx1024M -XX:MaxPermSize=512M" BUILDR_ARGS="clean package test=no JAVADOC=off"

RUN mkdir /build

COPY . /workspace

RUN jruby -S bundler install --gemfile=/workspace/Gemfile \
    && buildr $BUILDR_ARGS && cp /workspace/axis2-war/target/ode-axis2-war-1.3.7.war /build/ode.war

FROM tomcat:8.5-jre8
LABEL maintainer "Johannes Wettinger <jowettinger@gmail.com>, Michael Wurster <miwurster@gmail.com>, Michael Hahn <mhahn.dev@gmail.com>"

ARG DOCKERIZE_VERSION=v0.3.0

ENV TOMCAT_USERNAME admin
ENV TOMCAT_PASSWORD admin
ENV ENGINE_PLAN_PORT 9763

RUN rm /dev/random && ln -s /dev/urandom /dev/random \
    && wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

RUN apt-get update -qq && apt-get install -qqy \
        unzip \
    && rm -rf /var/lib/apt/lists/*

ADD tomcat-users.xml.tpl ${CATALINA_HOME}/conf/tomcat-users.xml.tpl
ADD manager.xml ${CATALINA_HOME}/conf/Catalina/localhost/manager.xml
ADD server.xml.tpl ${CATALINA_HOME}/conf/server.xml.tpl

COPY --from=builder /build/ode.war ${CATALINA_HOME}/webapps
RUN unzip ${CATALINA_HOME}/webapps/ode.war -d ${CATALINA_HOME}/webapps/ode

ADD axis2.xml.tpl ${CATALINA_HOME}/webapps/ode/WEB-INF/conf/axis2.xml.tpl

EXPOSE 9763

CMD dockerize -template ${CATALINA_HOME}/conf/tomcat-users.xml.tpl:${CATALINA_HOME}/conf/tomcat-users.xml \
    -template ${CATALINA_HOME}/conf/server.xml.tpl:${CATALINA_HOME}/conf/server.xml \
    -template ${CATALINA_HOME}/webapps/ode/WEB-INF/conf/axis2.xml.tpl:${CATALINA_HOME}/webapps/ode/WEB-INF/conf/axis2.xml \
    ${CATALINA_HOME}/bin/catalina.sh run

#
# Manually build by running:
#
#   docker build -t opentosca/engine-plan-ode .
#
# 
