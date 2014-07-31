PRioritizer
==========

A pull request watcher written in Scala.

Listens for RabbitMQ messages that contain an event id of pull request events.
For each event the owner and repository info is fetched from a MongoDB database.
If the server contains a local clone of the repository, the pull requests are prioritized.