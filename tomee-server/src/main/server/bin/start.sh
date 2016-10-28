#!/bin/sh

PRG="$0"

PRGDIR=`dirname "$PRG"`

ODE_SERVER_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

JAVA_OPTS="$JAVA_OPTS \
-Dode.server.home=$ODE_SERVER_HOME \
-Djava.naming.factory.initial=org.apache.openejb.core.LocalInitialContextFactory \
-Dderby.system.home=$ODE_SERVER_HOME"

JPDA_OPTS=""
# JPDA_OPTS="-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n"

CLASSPATH="$CLASSPATH":"$ODE_SERVER_HOME/conf"

for i in "$ODE_SERVER_HOME/lib"/*.jar; do
      CLASSPATH="$CLASSPATH":"$i"
done

java $JAVA_OPTS $JPDA_OPTS -classpath $CLASSPATH org.apache.ode.tomee.Main