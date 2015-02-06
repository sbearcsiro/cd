# copy this into visudo -f /etc/sudoers.d/tomcat7
# replace ala-ilife1 with the output of hostname
%tomcat7 ala-ilife1=(root)NOPASSWD:/usr/sbin/service tomcat7 *
