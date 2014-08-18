package utils

import java.io.{ByteArrayOutputStream, PrintStream}

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
