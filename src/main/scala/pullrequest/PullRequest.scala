package pullrequest

case class Base(label: String, sha: String, repo: String, owner: String)
case class Head(label: String, sha: String, repo: String, owner: String)

case class PullRequest(head: Head, base: Base)
