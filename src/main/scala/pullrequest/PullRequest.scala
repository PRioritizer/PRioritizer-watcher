package pullrequest

case class Base(label: String, sha: String, owner: String, repo: String)
case class Head(label: String, sha: String, owner: String, repo: String)

case class PullRequest(head: Head, base: Base)
