#!/bin/bash

aptitude -Ry install tomcat5.5-admin

/etc/init.d/tomcat5.5 stop

cd /usr/share/tomcat5.5/common/lib
rm /usr/share/tomcat5.5/common/lib/postgresql-8.3-605.jdbc4.jar
wget http://jdbc.postgresql.org/download/postgresql-8.3-605.jdbc4.jar

cd /etc/tomcat5.5/Catalina/localhost
rm -rf admin.xml host-manager.xml

cat << 'EOF' > /etc/tomcat5.5/Catalina/localhost/manager.xml
<Context path="/manager" docBase="/usr/share/tomcat5.5/server/webapps/manager"
        debug="0" privileged="true">
  <ResourceLink name="users" global="UserDatabase"
                type="org.apache.catalina.UserDatabase"/>
  <Valve className="org.apache.catalina.valves.RemoteAddrValve"
         allow="192.168.14.1" />
</Context>
EOF

cat << 'EOF' > /etc/tomcat5.5/server.xml
<Server port="8005" shutdown="SHUTDOWN">
  <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
       description="User database that can be updated and saved"
           factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
          pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>
  <Service name="Catalina">
    <Connector port="80" maxHttpHeaderSize="8192"
               maxThreads="1000" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" redirectPort="8443" acceptCount="100"
               connectionTimeout="20000" disableUploadTimeout="true" />
    <Connector port="443" maxHttpHeaderSize="8192"
               maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" disableUploadTimeout="true"
               acceptCount="100" scheme="https" secure="true"
               clientAuth="true" sslProtocol="TLS" keyAlias="tomcat"
               keystoreFile="conf/keystore" truststoreFile="conf/truststore" />
    <Engine name="Catalina" defaultHost="localhost">
      <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
             resourceName="UserDatabase"/>
      <Host name="localhost" appBase="webapps"
            unpackWARs="true" autoDeploy="false" />
    </Engine>
  </Service>
</Server>
EOF

cat << 'EOF' > /etc/tomcat5.5/tomcat-users.xml
<tomcat-users>
  <role rolename="manager"/>
  <user username="portus" password="portus" roles="manager"/>
</tomcat-users>
EOF

cp ~chimera/bin/setup/*store /etc/tomcat5.5
chmod 700 /etc/tomcat5.5/*store

chown -R nobody.nogroup /var/lib/tomcat5.5/webapps
chown -R nobody.nogroup /var/lib/tomcat5.5/temp
chown -R nobody.nogroup /etc/tomcat5.5
chown -R nobody.nogroup /var/log/tomcat5.5
chown -R nobody.nogroup /var/cache/tomcat5.5

cat << 'EOF' > /etc/default/tomcat5.5
# Run Tomcat as this user ID. Not setting this or leaving it blank will use the
# default of tomcat55.
TOMCAT5_USER=nobody

# The home directory of the Java development kit (JDK). You need at least
# JDK version 1.4. If JAVA_HOME is not set, some common directories for
# the Sun JDK, various J2SE 1.4 versions, and the free runtimes
# java-gcj-compat-dev and kaffe are tried.
#JAVA_HOME=/usr/lib/jvm/java-6-sun

# Directory for per-instance configuration files and webapps. It contain the
# directories conf, logs, webapps, work and temp. See RUNNING.txt for details.
# Default: /var/lib/tomcat5.5
#CATALINA_BASE=/var/lib/tomcat5.5

# Arguments to pass to the Java virtual machine (JVM).
JAVA_OPTS="-Djava.awt.headless=true -Xmx386M"

# Java compiler to use for translating JavaServer Pages (JSPs). You can use all
# compilers that are accepted by Ant's build.compiler property.
#JSP_COMPILER=jikes

# Use the Java security manager? (yes/no, default: yes)
# WARNING: Do not disable the security manager unless you understand
# the consequences!
# NOTE: java-gcj-compat-dev currently doesn't support a security
# manager.
TOMCAT5_SECURITY=no
EOF

/etc/init.d/tomcat5.5 start
