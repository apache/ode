#! /bin/sh

cygwin=false;
darwin=false;
case "`uname`" in
    Darwin*) 
	darwin=true
	if [ -z "$JAVA_HOME" ] ; then
	    JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
	fi
	;;
    CYGWIN*) 
	cygwin=true 
	;;
esac

## Try to find our home directory 
command="$0"
progname=`basename "$0"`
# need this for relative symlinks
while [ -h "$command" ] ; do
    ls=`ls -ld "$command"`
    link=`expr "$command" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
	command="$link"
    else
	command=`dirname "$command"`"/$link"
    fi
done

ODE_BIN=`dirname "$command"`
ODE_BIN=`cd "$ODE_BIN" && pwd`

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$ODE_BIN" ] && ODE_BIN=`cygpath --unix "$ODE_BIN"`
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

ODE_HOME=`cd "$ODE_BIN/.." && pwd`
LIB="$ODE_HOME/lib"
ETC="$ODE_HOME/etc"


if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ ! -d "$JAVA_HOME" ] ; then
 echo "Error: JAVA_HOME is not defined correctly (no such directory)."
 exit 1
fi

# Add user-specified classpath. 
LOCALCLASSPATH="$ODE_CLASSPATH"

# Add Ode libraries
for f in $LIB/*.jar
do
  LOCALCLASSPATH=$LOCALCLASSPATH:$f
done

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    ODE_HOME=`cygpath --windows "$ODE_HOME"`
    JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
    LOCALCLASSPATH=`cygpath --path --windows "$LOCALCLASSPATH"`
    CYGHOME=`cygpath --windows "$HOME"`
    LIB=`cygpath --windows "$LIB"`
    ODE_BIN=`cygpath --windows "$ODE_BIN"`
    ETC=`cygpath --windows "$ETC"`
fi

exec "$JAVACMD" $ODE_JAVAOPTS -cp "$LOCALCLASSPATH" org.apache.ode.tools.sendsoap.cline.HttpSoapSender "$@"

