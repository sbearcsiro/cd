#!/bin/sh
# copy this along with built jar assembly to /opt/atlas/cd/

# overwrite settings from default file
if [ -f "/etc/default/cd" ]; then
        . "/etc/default/cd"
fi
sudo -u tomcat7 java -jar cd-assembly-1.0.jar $JVM_OPTS $CONFIG_OPTS "$@"
