#!/bin/bash
#
# Startup script.
#

#
# Determine if JAVA_HOME is set and if so then use it
#
if [ -z "$JAVA_HOME" ] ;  then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BINDIR=`dirname $JAVA`
  JAVA_HOME=$JAVA_BINDIR/..
fi

if [ "$JAVACMD" = "" ] ; then
   # it may be defined in env - including flags!!
   JAVACMD=$JAVA_HOME/bin/java
fi

# Main.java has hard coded config values so this script must be run from
# altprofile/bin (any better ideas ?)
EXAMPLE_HOME=..

#
# Build the runtime classpath
#
for i in ${EXAMPLE_HOME}/lib/*.jar ; do
    CP=${CP}:$i
done

CP=${CP}:${EXAMPLE_HOME}/build/classes

if [ "$TERM" = "cygwin" ] ; then
	CP=`cygpath --windows --path $CP`
fi

echo $CP

# Run the example application
$JAVACMD -classpath "$CP" org.apache.avalon.fortress.examples.viewer.Main $@

