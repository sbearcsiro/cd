#!/bin/sh

if [ ! -z $JAVA_HOME ]; then
    JAVA_COMMAND=$JAVA_HOME/bin/java
elif cmd=$(command -v java); then
    JAVA_COMMAND=$cmd
else
    echo "Couldn't find java!"
fi

SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
$JAVA_COMMAND $SBT_OPTS -jar `dirname $0`/sbt/sbt-launch.jar "$@"