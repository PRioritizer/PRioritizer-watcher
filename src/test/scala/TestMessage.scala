import queue.Message

case class TestMessage(contents: String) extends Message {
  def acknowledge(success: Boolean) = {}
}
