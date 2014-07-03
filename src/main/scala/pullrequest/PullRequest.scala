package pullrequest

case class Base(label: String, sha: String, owner: String, repository: String)
case class Head(label: String, sha: String, owner: String, repository: String)

case class PullRequest(head: Head, base: Base)
