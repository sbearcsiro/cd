#!/bin/sh
set -e
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi
mkdir /tmp/deploy-setup
pushd /tmp/deploy-setup
git clone https://github.com/sbearcsiro/deploy
cd deploy
./sbtw.sh assembly
mkdir -p /opt/atlas/deploy/
mkdir -p /usr/local/etc/atlas
cp target/scala-2.11/deploy-assembly-1.0.jar /opt/atlas/deploy/deploy-assembly-1.0.jar
cp ubuntu/deploy.sh /opt/atlas/deploy/deploy.sh
chmod a+x /opt/atlas/deploy/deploy.sh
cp ubuntu/deploy.conf /etc/init/deploy.conf
cp ubuntu/config /usr/local/etc/atlas/deploy.conf
sed "s/{{hostname}}/`hostname`/" ubuntu/tomcat7-sudoers.d > /etc/sudoers.d/tomcat7
popd
rm -r /tmp/deploy-setup
echo "Install complete"
echo "----------------"
echo 
echo "Next steps:"
echo "1. Setup reverse proxy from http(s)://appname.ala.org.au/deploy/* to http://localhost:7070/deploy/*"
echo "2. Customise /usr/local/etc/atlas/deploy.conf for your application"
echo "3. Start the deploy service (service deploy start)"
echo "4. Check logs to ensure it worked: /var/logs/upstart/deploy.log"