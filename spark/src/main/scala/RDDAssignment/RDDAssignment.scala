package RDDAssignment

import org.apache.spark.rdd.RDD
import utils.{Commit, File, Stats}

import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID

object RDDAssignment {


  /**
   * Reductions are often used in data processing in order to gather more useful data out of raw data. In this case
   * we want to know how many commits a given RDD contains.
   *
   * @param commits RDD containing commit data.
   * @return Long indicating the number of commits in the given RDD.
   */
  def assignment_1(commits: RDD[Commit]): Long = commits.count()

  /**
   * We want to know how often programming languages are used in committed files. We want you to return an RDD containing Tuples
   * of the used file extension, combined with the number of occurrences. If no filename or file extension is used we
   * assume the language to be 'unknown'.
   *
   * @param commits RDD containing commit data.
   * @return RDD containing tuples indicating the programming language (extension) and number of occurrences.
   */
  def assignment_2(commits: RDD[Commit]): RDD[(String, Long)] = {
    commits.flatMap(x => x.files).groupBy(x => {

      val regex = ".*\\.(.*)".r

      x.filename match {
        case Some(regex(p)) => p
        case _ => "unknown"

      }
    }).map(x => (x._1, x._2.size.toLong))
  }


  /**
   * Competitive users on GitHub might be interested in their ranking in the number of commits. We want you to return an
   * RDD containing Tuples of the rank (zero indexed) of a commit author, a commit author's name and the number of
   * commits made by the commit author. As in general with performance rankings, a higher performance means a better
   * ranking (0 = best). In case of a tie, the lexicographical ordering of the usernames should be used to break the
   * tie.
   *
   * @param commits RDD containing commit data.
   * @return RDD containing the rank, the name and the total number of commits for every author, in the ordered fashion.
   */
  def assignment_3(commits: RDD[Commit]): RDD[(Long, String, Long)] = {
    commits.map(x => (x.commit.author.name, 1))
      .reduceByKey((acc, x) => acc + x).
      sortBy(x => x._1.toLowerCase())
      .sortBy(x => x._2, false).zipWithIndex()
      .map(x => (x._2, x._1._1, x._1._2))

  }

  /**
   * Some users are interested in seeing an overall contribution of all their work. For this exercise we want an RDD that
   * contains the committer's name and the total number of their commit statistics. As stats are Optional, missing Stats cases should be
   * handled as "Stats(0, 0, 0)".
   *
   * Note that if a user is given that is not in the dataset, then the user's name should not occur in
   * the resulting RDD.
   *
   * @param commits RDD containing commit data.
   * @return RDD containing committer names and an aggregation of the committers Stats.
   */
  def assignment_4(commits: RDD[Commit], users: List[String]): RDD[(String, Stats)] = {

    def sum(x: Stats, y: Stats): Stats = Stats(x.total + y.total, y.additions + x.additions, x.deletions + y.deletions)

    commits.map(x => (x.commit.committer.name, x.stats.getOrElse(Stats(0, 0, 0))))
      .groupBy(t => t._1)
      .map(t => t._2.reduce((x, y) => (x._1, sum(x._2, y._2))))
      .filter(x => users.contains(x._1))
  }


  /**
   * There are different types of people: those who own repositories, and those who make commits. Although Git blame command is
   * excellent in finding these types of people, we want to do it in Spark. As the output, we require an RDD containing the
   * names of commit authors and repository owners that have either exclusively committed to repositories, or
   * exclusively own repositories in the given commits RDD.
   *
   * Note that the repository owner is contained within GitHub URLs.
   *
   * @param commits RDD containing commit data.
   * @return RDD of Strings representing the usernames that have either only committed to repositories or only own
   *         repositories.
   */
  def assignment_5(commits: RDD[Commit]): RDD[String] = {
    val commiters = commits.map(x => x.commit.author.name).distinct()
    val owners = commits.map(x => x.url.split("/").take(5).last).distinct()
    owners.union(commiters).subtract(owners.intersection(commiters))
  }

  /**
   * Sometimes developers make mistakes and sometimes they make many many of them. One way of observing mistakes in commits is by
   * looking at so-called revert commits. We define a 'revert streak' as the number of times `Revert` occurs
   * in a commit message. Note that for a commit to be eligible for a 'revert streak', its message must start with `Revert`.
   * As an example: `Revert "Revert ...` would be a revert streak of 2, whilst `Oops, Revert Revert little mistake`
   * would not be a 'revert streak' at all.
   *
   * We require an RDD containing Tuples of the username of a commit author and a Tuple containing
   * the length of the longest 'revert streak' of a user and how often this streak has occurred.
   * Note that we are only interested in the longest commit streak of each author (and its frequency).
   *
   * @param commits RDD containing commit data.
   * @return RDD of Tuples containing a commit author's name and a Tuple which contains the length of the longest
   *         'revert streak' as well its frequency.
   */
  def assignment_6(commits: RDD[Commit]): RDD[(String, (Int, Int))] = {
    def Rcount(revert: String): Int = {
      if (!revert.startsWith("Revert"))
        return 0
      revert.split("Revert").size - 1
    }

    def Maxim(a: (Int, Int), b: (Int, Int)): (Int, Int) = {
      if (a._1 > b._1)
        return a
      b
    }

    commits.map(x => (x.commit.author.name, Rcount(x.commit.message)))
      .groupBy(identity)
      .filter(x => x._1._2 > 0)
      .map(x => (x._1._1, (x._1._2, x._2.size))).reduceByKey((x, y) => Maxim(x, y))
  }


  /**
   * !!! NOTE THAT FROM THIS EXERCISE ON (INCLUSIVE), EXPENSIVE FUNCTIONS LIKE groupBy ARE NO LONGER ALLOWED TO BE USED !!
   *
   * We want to know the number of commits that have been made to each repository contained in the given RDD. Besides the
   * number of commits, we also want to know the unique committers that contributed to each of these repositories.
   *
   * In real life these wide dependency functions are performance killers, but luckily there are better performing alternatives!
   * The automatic graders will check the computation history of the returned RDDs.
   *
   * @param commits RDD containing commit data.
   * @return RDD containing Tuples with the repository name, the number of commits made to the repository as
   *         well as the names of the unique committers to this repository.
   */
  def assignment_7(commits: RDD[Commit]): RDD[(String, Long, Iterable[String])] = commits
    .map(x => (x.url.split("/").take(6).last, (1, x.commit.committer.name :: Nil)))
    .reduceByKey((key, value) => (key._1 + value._1, key._2 ::: value._2)).map(t => (t._1, t._2._1.toLong, t._2._2.distinct))

  /**
   * Return an RDD of Tuples containing the repository name and all the files that are contained in this repository.
   * Note that the file names must be unique, so if a file occurs multiple times (for example, due to removal, or new
   * addition), the newest File object must be returned. As the filenames are an `Option[String]`, discard the
   * files that do not have a filename.
   *
   * To reiterate, expensive functions such as groupBy are not allowed.
   *
   * @param commits RDD containing commit data.
   * @return RDD containing the files in each repository as described above.
   */
  def assignment_8(commits: RDD[Commit]): RDD[(String, Iterable[File])] = {

    commits.map(x => (x.url.split("/").take(6).last, x.files))
      .flatMap(x => x._2.map(f => (x._1, f)))
      .filter(x => x._2.filename.isDefined)
      .map(x => ((x._1, x._2.filename), x._2))
      .reduceByKey((a, b) => b)
      .map(x => (x._1._1, x._2))
      .aggregateByKey(List[File]())((acc, x) => acc ++ List(x), (a, b) => a ++ b)
      .map(x => (x._1, x._2.toIterable))


  }

  /**
   * For this assignment you are asked to find all the files of a single repository. This is in order to create an
   * overview of each file by creating a Tuple containing the file name, all corresponding commit SHA's,
   * as well as a Stat object representing all the changes made to the file.
   *
   * To reiterate, expensive functions such as groupBy are not allowed.
   *
   * @param commits RDD containing commit data.
   * @return RDD containing Tuples representing a file name, its corresponding commit SHA's and a Stats object
   *         representing the total aggregation of changes for a file.
   */
  def assignment_9(commits: RDD[Commit], repository: String): RDD[(String, Seq[String], Stats)] = {
    def Helper(x: Stats, y: Stats): Stats = Stats(x.total + y.total, x.additions + y.additions, x.deletions + y.deletions)

    commits.filter(x => x.url.split("/")
      .take(6).last.contains(repository))
      .map(x => (x.files.map(t => (t, Seq(x.sha)))))
      .flatMap(x => x)
      .map(x => (x._1.filename.get, (x._2, Stats(x._1.changes, x._1.additions, x._1.deletions))))
      .reduceByKey((x, y) => ((x._1 ++ y._1, Helper(x._2, y._2))))
      .map(x => (x._1, x._2._1, x._2._2))


  }

  /**
   * We want to generate an overview of the work done by a user per repository. For this we want an RDD containing
   * Tuples with the committer's name, the repository name and a `Stats` object containing the
   * total number of additions, deletions and total contribution to this repository.
   * Note that since Stats are optional, the required type is Option[Stat].
   *
   * To reiterate, expensive functions such as groupBy are not allowed.
   *
   * @param commits RDD containing commit data.
   * @return RDD containing Tuples of the committer's name, the repository name and an `Option[Stat]` object representing additions,
   *         deletions and the total contribution to this repository by this committer.
   */
  def assignment_10(commits: RDD[Commit]): RDD[(String, String, Option[Stats])] = {
    def Helper(x: Stats, y: Stats): Stats
    = Stats(x.total + y.total, x.additions + y.additions, x.deletions + y.deletions)

    commits.map(x => ((x.url.split("/")
      .apply(5), x.commit.committer.name), x.stats.getOrElse(Stats(0, 0, 0))))
      .reduceByKey((x, y) => Helper(x, y)).map(x => (x._1._2, x._1._1, Some(x._2)))
  }


  /**
   * Hashing function that computes the md5 hash of a String and returns a Long, representing the most significant bits of the hashed string.
   * It acts as a hashing function for repository name and username.
   *
   * @param s String to be hashed, consecutively mapped to a Long.
   * @return Long representing the MSB of the hashed input String.
   */
  def md5HashString(s: String): Long = {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    UUID.nameUUIDFromBytes(hashedString.getBytes()).getMostSignificantBits
  }

}