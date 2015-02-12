# copy this into visudo -f /etc/sudoers.d/tomcat7
# replace {{hostname}} with the output of hostname
%tomcat7 {{hostname}}=(root)NOPASSWD:/usr/sbin/service tomcat7 *
