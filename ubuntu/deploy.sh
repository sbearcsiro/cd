#!/bin/sh
# copy this along with built jar assembly to /opt/atlas/cd/

JVM_OPTS=""

CONFIG_OPTS=""
if [ -f /usr/local/etc/atlas/deploy.conf ]; then
        CONFIG_OPTS="-Dconfig.file=/usr/local/etc/atlas/deploy.conf"
fi
if [ -f /etc/atlas/cd.conf ]; then
        CONFIG_OPTS="-Dconfig.file=/etc/atlas/deploy.conf"
fi

# overwrite settings from default file
if [ -f "/etc/default/deploy" ]; then
        . "/etc/default/deploy"
fi

if [ ! -z $JAVA_HOME ]; then
    JAVA_COMMAND=$JAVA_HOME/bin/java
elif cmd=$(command -v java); then
    JAVA_COMMAND=$cmd
else
    echo "Couldn't find java!"
fi

DIR=$( cd "$( dirname "$0" )" && pwd )
$JAVA_COMMAND $JVM_OPTS $CONFIG_OPTS -jar ${DIR}/deploy-assembly-1.0.jar $@

