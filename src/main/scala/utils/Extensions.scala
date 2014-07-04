package utils

import java.io.{PrintStream, ByteArrayOutputStream}

object Extensions {
  implicit class EnrichException(ex: Throwable) {
    def stackTraceToString: String = {
      val output = new ByteArrayOutputStream()
      val stream = new PrintStream(output)
      ex.printStackTrace(stream)
      output.toString("UTF-8").trim
    }
  }
}
