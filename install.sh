#!/bin/sh
set -e
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

mkdir -p /tmp/deploy-setup
pushd /tmp/deploy-setup

echo "Downloading files..."

wget "https://github.com/sbearcsiro/deploy/releases/download/v1.0.0/deploy-assembly-1.0.jar"
wget "https://raw.githubusercontent.com/sbearcsiro/deploy/master/ubuntu/config"
wget "https://raw.githubusercontent.com/sbearcsiro/deploy/master/ubuntu/default"
wget "https://raw.githubusercontent.com/sbearcsiro/deploy/master/ubuntu/deploy.conf"
wget "https://raw.githubusercontent.com/sbearcsiro/deploy/master/ubuntu/deploy.sh"
wget "https://raw.githubusercontent.com/sbearcsiro/deploy/master/ubuntu/tomcat7-sudoers.d"

echo "Making directories..."

mkdir -p /opt/atlas/deploy/
mkdir -p /usr/local/etc/atlas

echo "Copying files..."

cp deploy-assembly-1.0.jar /opt/atlas/deploy/deploy-assembly-1.0.jar
cp deploy.sh /opt/atlas/deploy/deploy.sh
chmod a+x /opt/atlas/deploy/deploy.sh
cp deploy.conf /etc/init/deploy.conf
cp config /usr/local/etc/atlas/deploy.conf
sed "s/{{hostname}}/`hostname`/" tomcat7-sudoers.d > /etc/sudoers.d/tomcat7

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
