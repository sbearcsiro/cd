# deploy
Simple HTTP agent for continuous deployment of Tomcat + Postgres apps

##What it does

 * Downloads the app war file from a maven repo to `/tmp/deploy/`
 * Shuts down the tomcat7 service
 * Backs up the postgres database to `/tmp/deploy/backup`
 * Backs up the existing war file to `/tmp/deploy/backup`
 * Copies the new war file into `/var/lib/tomcat7/webapps/ROOT.war`
 * Deletes the existing uncompressed war file from `/var/lib/tomcat7/webapps/ROOT`
 * Starts the tomcat7 service

**NOTE:** All paths can be configured (see below)

## Running

Once installed, simply send an HTTP POST to the https://appname.ala.org.au/deploy/version-number with an HTTP Header `X-DEPLOY-KEY` set
to the password given in the `deploy.conf` file.  Eg:

```
curl -X POST --header "X-DEPLOY-KEY: ${DEPLOY_KEY}" https://${APP_NAME}-dev.ala.org.au/deploy/${APP_VERSION}'
```

Or, in your `.travis.yml`s `after_sucess:` section:

```
- '[ "${TRAVIS_BRANCH}" = "develop" ] && travis_retry curl -X POST --header "X-DEPLOY-KEY:
  ${DEPLOY_KEY}" https://volunteer-dev.ala.org.au/deploy/${APP_VERSION}'
```


##Auto install

`curl https://github.com/sbearcsiro/cd/raw/master/install.sh | sudo bash`

###Customise:

Edit `/usr/local/etc/deploy.conf` to set up for your app, eg:

```
deploy {
  app.name = "maven-artifact-name" // This is the maven (war) artifact to be deployed
  api.key = "abc123" // This is the password for the X-DEPLOY-KEY HTTP Header

  db.name = "postgres-db-name" // The name of the database to backup
  db.host = "localhost" // The hostname of the database server
  db.username = "postgres" // The database username
  db.password = "password" // The database password

  hipchat.enabled = true // true to enable hipchat integration, false to disable
  hipchat.key = blahblahblahblah // hipchat user or room oauth2 token
  hipchat.room = "application room" // hipchat room id or name
}
```

This config file uses the typesafe config library's HOCON format.

You can also override some of the default values with the following keys:

```

deploy {
  port = 7070 # Port to run the agent on
  group   = "au.org.ala" # The group id
  snapshot.regex = ".*-SNAPSHOT$" # regex to use to determine whether it's in the snapshot repo or not

  download.url = "http://nexus.ala.org.au/service/local/artifact/maven/redirect?r={repo}&g={group}&a={appName}&v={version}&p=war" # URL to download the artifact from
  download.dir = "/tmp/deploy" # Where to stage the downloaded artifact
  backup.dir = "/tmp/deploy/backup" # Directory to put backup files
  catalina.base = "/var/lib/tomcat7" # Catalina base directory
  catalina.webapps = "webapps" # The webapps directory for the app, relative to the Catalina base directory
  webapp.context = "ROOT" # The webapp context path
}

```

### Reverse proxy

You should also reverse proxy from https://appname.ala.org.au/deploy/* to http://localhost:7070/deploy/*


##Manual install

###Build:

`git clone` the repo then `sbt assembly` (or `./sbtw assembly` if `sbt` is not installed)

###Copy:

 * `target/scala-2.11/deploy-assembly-1.0.jar` to `/opt/atlas/deploy/deploy-assembly-1.0.jar`
 * `deploy/deploy.sh` to `/opt/atlas/deploy/deploy.sh`
 * `deploy/deploy.conf` to `/etc/init/deploy.conf` (Ubuntu upstart script)
 * `deploy/config` to `/usr/local/etc/atlas/deploy.conf`
 * `deploy/tomcat7-sudoers.d` to `/etc/sudoers.d/tomcat7` (Replace `{{hostname}}` with the output of running `hostname`)
