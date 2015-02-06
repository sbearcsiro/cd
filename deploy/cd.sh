#!/bin/sh
# copy this along with built jar assembly to /opt/atlas/cd/

# overwrite settings from default file
if [ -f "/etc/default/cd" ]; then
        . "/etc/default/cd"
fi
DIR=$( cd "$( dirname "$0" )" && pwd )
java $JVM_OPTS $CONFIG_OPTS -jar ${DIR}/cd-assembly-1.0.jar $@

