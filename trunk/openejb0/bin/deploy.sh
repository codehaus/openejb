#!/bin/sh

# $Id$

# Contributions by:
#     David Blevins <david.blevins@visi.com>
#     Pizer Chen  <iceant@21cn.com>

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`type -p java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
fi
JAVA=$JAVA_HOME/bin/java

if [ -z "$OPENEJB_HOME" ] ; then
  OPENEJB_HOME=$PWD
fi

# PS stands for PATH SEPERATOR
PS=":"

case "`uname`" in
  CYGWIN*) PS=";";;
esac

# Setup Classpath

CP=
#==================================
# put dist/*.jar file to $CP
for i in $OPENEJB_HOME/dist/*.jar;
do 
    CP=$i${PS}$CP
done

echo "--------------SUPPORT INFO-------------"
echo "`uname -srv`"
echo "Using JAVA_HOME:     $JAVA_HOME"
echo "Using OPENEJB_HOME:  $OPENEJB_HOME"
echo "."

CP=$JAVA_HOME/lib/tools.jar${PS}${CP}
CLASSPATH=$CP

$JAVA -cp $CLASSPATH -Dopenejb.home=$OPENEJB_HOME org.openejb.alt.config.Deploy $@

