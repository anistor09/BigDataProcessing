package dataset

import dataset.util.Commit.Commit

import java.text.SimpleDateFormat
import java.util.{Date, SimpleTimeZone}
import scala.math.Ordering.Implicits._

/**
 * Use your knowledge of functional programming to complete the following functions.
 * You are recommended to use library functions when possible.
 *
 * The data is provided as a list of `Commit`s. This case class can be found in util/Commit.scala.
 * When asked for dates, use the `commit.commit.committer.date` field.
 *
 * This part is worth 40 points.
 */
object Dataset {


  /** Q23 (4p)
   * For the commits that are accompanied with stats data, compute the average of their additions.
   * You can assume a positive amount of usable commits is present in the data.
   *
   * @param input the list of commits to process.
   * @return the average amount of additions in the commits that have stats data.
   */
  def avgAdditions(input: List[Commit]): Int = input.flatMap(x=> x.stats).map(x=>x.additions).sum/(input.flatMap(x=> x.stats).size)



  /** Q24 (4p)
   * Find the hour of day (in 24h notation, UTC time) during which the most javascript (.js) files are changed in commits.
   * The hour 00:00-00:59 is hour 0, 14:00-14:59 is hour 14, etc.
   * NB!filename of a file is always defined.
   * Hint: for the time, use `SimpleDateFormat` and `SimpleTimeZone`.
   *
   * @param input list of commits to process.
   * @return the hour and the amount of files changed during this hour.
   */
  def jsTime(input: List[Commit]): (Int, Int) = {

    val simpleDateFormat = new SimpleDateFormat("HH")
    simpleDateFormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME,"UTC"))

    input.map(x=> (simpleDateFormat.format(x.commit.committer.date).toInt,x.files.count(_.filename.get.endsWith(".js"))))
      .groupBy(_._1)
      .map(a=> (a._1, a._2.map(_._2).sum))
      .maxBy(_._2)
  }


  /** Q25 (5p)
   * For a given repository, output the name and amount of commits for the person
   * with the most commits to this repository.
   * For the name, use `commit.commit.author.name`.
   *
   * @param input the list of commits to process.
   * @param repo  the repository name to consider.
   * @return the name and amount of commits for the top committer.
   *
   *
   */

    def urlSplitter( x : String): String ={
      val y = x.split("/")

      y(4)+"/"+y(5)

    }
  def topCommitter(input: List[Commit], repo: String): (String, Int) =
  {
    input
      .filter(x=> x.url.contains(repo))
      .groupBy(x=>x.commit.author.name)
      .toList
      .map(x=>(x._1, x._2.size))
      .maxBy(x=>x._2)

  }

  /** Q26 (9p)
   * For each repository, output the name and the amount of commits that were made to this repository in 2019 only.
   * Leave out all repositories that had no activity this year.
   *
   * @param input the list of commits to process.
   * @return a map that maps the repo name to the amount of commits.
   *
   *         Example output:
   *         Map("KosDP1987/students" -> 1, "giahh263/HQWord" -> 2)
   */
  def commitsPerRepo(input: List[Commit]): Map[String, Int] = {
    input.filter(x=>isCommitedIn2019(x.commit.committer.date)).groupBy(x=> urlSplitter(x.url)).map(x=>(x._1,x._2.size))
  }

  def isCommitedIn2019( date: Date): Boolean ={
    val dateFormat = new SimpleDateFormat(
      "dd-MMM-yyyy HH:mm:ss")

    val stringDate = dateFormat.format(date)

    if(stringDate.contains("2019"))
      true
      else
      false
  }



  /** Q27 (9p)
   * Derive the 5 file types that appear most frequent in the commit logs.
   * NB!filename of a file is always defined.
   * @param input the list of commits to process.
   * @return 5 tuples containing the file extension and frequency of the most frequently appeared file types, ordered descendingly.
   */
  def topFileFormats(input: List[Commit]): List[(String, Int)] = {
    val possibleValues = input
      .flatMap(x=> x.files.map( y=> y.filename.get.split("\\.").last))

    val distinctPossibleValues = input
      .flatMap(x=> x.files.map( y=> y.filename.get.split("\\.").last)).distinct

    distinctPossibleValues
      .map( x=>(x,possibleValues.count(y=>y.equals(x))))

      .sortBy(x=>x._2)
      .reverse.
      take(5)


  }


  /** Q28 (9p)
   *
   * A day has different parts:
   * Morning 5 am to 12 pm (noon)
   * Afternoon 12 pm to 5 pm.
   * Evening 5 pm to 9 pm.
   * Night 9 pm to 4 am.
   *
   * Which part of the day was the most productive in terms of commits ?
   * Return a tuple with the part of the day and the number of commits
   *
   * Hint: for the time, use `SimpleDateFormat` and `SimpleTimeZone`.
   */
  def mostProductivePart(input: List[Commit]): (String, Int) = {
    val simpleDateFormat = new SimpleDateFormat("HH")
    simpleDateFormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME,"UTC"))
    input.reduce
    input.map(x=> {
      val time = simpleDateFormat.format(x.commit.committer.date).toInt

      if (time < 12 && time >= 5)
        ("morning", 1)
      else if (12 <= time && time < 17)
        ("afternoon", 1)
      else if (17 <= time && time < 21)
        ("evening", 1)
      else
        ("night", 1)
    }).groupBy(_._1)
      .map(x=> (x._1, x._2.map(_._2).sum))
    .maxBy(x=> x._2)

    }

}
