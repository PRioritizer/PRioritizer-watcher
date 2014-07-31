package queue

trait Message {
  def contents: String
  def acknowledge(success: Boolean = true)
}
