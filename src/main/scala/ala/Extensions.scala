import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, SimpleFileVisitor, Files, Path}
import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.scalalogging.StrictLogging

import scala.sys.process.ProcessBuilder
import scala.util.matching.Regex

package object ala extends StrictLogging {
  implicit class RichDate(val date: Date) {
    def iso8061 = {
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(date)
    }
  }

  @throws(classOf[IOException])
  def run(command: ProcessBuilder) = {
    val exitCode = command.!
    if (exitCode != 0) throw new IOException(s"${command.toString} returned non-zero value $exitCode")
  }

  @throws(classOf[IOException])
  def rmr(path: Path) = {
    Files.walkFileTree(path, new SimpleFileVisitor[Path]() {
      override def visitFile(file : Path, attrs : BasicFileAttributes) : FileVisitResult = {
        logger.debug(s"Deleting file $file")
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir : Path, exc : IOException) : FileVisitResult = {
        logger.debug(s"Deleting directory $dir")
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }

    })
  }

  def replaceTemplates(text: String,
                       templates: Map[String, String]): String =
    """\{([^{}]*)\}""".r replaceSomeIn ( text,  { case Regex.Groups(name) => templates get name } )
}
