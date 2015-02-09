package ala

import java.io.{File, IOException}
import java.net.URL
import java.nio.file._
import java.util.Date

import com.typesafe.scalalogging.StrictLogging

import scala.sys.process._
import scala.util.matching.Regex

class DeployImpl ( val group: String = "au.org.ala",
              val appName: String = "volunteer-portal",
              val dbName: String = "volunteerstest",
              val dbHost: String = "localhost",
              val dbUsername: String = "postgres",
              val dbPassword: String = "password",
              val snapshot: Regex = ".*-SNAPSHOT$".r,
              val downloadUrl: String = "http://nexus.ala.org.au/service/local/artifact/maven/redirect?r={repo}&g={group}&a={appName}&v={version}&p=war",
              val downloadDir: String = "/tmp/deploy",
              val backupDir: String = "/tmp/deploy/backup",
              val catalinaBase: String = "/var/lib/tomcat7",
              val catalinaWebapps: String = "webapps",
              val webappContext: String = "ROOT") extends Deploy with StrictLogging {

  val outDir = new File(downloadDir)
  if (!outDir.exists() && !outDir.mkdirs()) throw new IOException(s"Couldn't create directories $downloadDir")
  val backupDirFile = new File(backupDir)
  if (!backupDirFile.exists() && !backupDirFile.mkdirs()) throw new IOException(s"Couldn't create directories $backupDir")
  
  @throws(classOf[IOException])
  def apply(version: String) : Unit = {
    
    val now = new Date().iso8061
    val outFile = new File(outDir, s"$appName-$version.war")
    val warDir = Paths.get(s"$catalinaBase/$catalinaWebapps/$webappContext")
    val warFile = new File(s"$catalinaBase/$catalinaWebapps/$webappContext.war")
    val backupPath = Paths.get(s"$backupDir/$appName-backup-$now.war")
    val dbBackup = new File(s"$backupDir/$appName-$now.dump")
    
    val repo = version match {
      case snapshot() => "snapshots"
      case _ => "releases"
    }
    
    if (outFile.exists() && !outFile.delete()) throw new IOException(s"Couldn't delete $outFile")
    val mavenUrl = replaceTemplates(downloadUrl, Map("repo" -> repo, "group" -> group, "appName" -> appName, "version" -> version))
    val url = new URL(mavenUrl)

    logger.info(s"Downloading $mavenUrl to $outFile")
    run(url #> outFile)
    logger.info(s"Downloading $mavenUrl to $outFile complete")
    
    if (!outFile.exists()) throw new IOException(s"Could not download $outFile")
    
    run(Seq("sudo", "/usr/sbin/service", "tomcat7", "stop"))
    logger.info(s"Backing up $dbName...")
    run(Process(Seq("pg_dump", "-Fc", "-h", dbHost, "-U", dbUsername, dbName), None, "PGPASSWORD" -> dbPassword) #> dbBackup)

    logger.info(s"Copying $warFile to $backupPath...")
    Files.copy(warFile.toPath, backupPath, StandardCopyOption.REPLACE_EXISTING)
    logger.info(s"Copying $outFile to $warFile...")
    Files.copy(outFile.toPath, warFile.toPath, StandardCopyOption.REPLACE_EXISTING)

    logger.info(s"Removing existing app $warDir...")
    rmr(warDir)
    
    run(Seq("sudo", "/usr/sbin/service", "tomcat7", "start"))
  }
}

object Deploy {
  private val deploy = new DeployImpl()
  
  @throws(classOf[IOException])
  def apply(version: String) : Unit = {
    deploy(version)
  }
}
