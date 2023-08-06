import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, TumblingEventTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import util.Protocol.{Commit, CommitSummary}
import util.{CommitGeoParser, CommitParser}

import java.text.SimpleDateFormat
import java.util.Date


/** Do NOT rename this class, otherwise autograding will fail. * */
object FlinkAssignment {

  val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

  def main(args: Array[String]): Unit = {

    /**
     * Setups the streaming environment including loading and parsing of the datasets.
     *
     * DO NOT TOUCH!
     */
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    // Read and parses commit stream.
    val commitStream =
      env
        .readTextFile("data/flink_commits.json")
        .map(new CommitParser)

    // Read and parses commit geo stream.
    val commitGeoStream =
      env
        .readTextFile("data/flink_commits_geo.json")
        .map(new CommitGeoParser)

    /** Use the space below to print and test your questions. */


    question_six(commitStream).print()

    /** Start the streaming environment. * */
    env.execute()
  }

  /** Dummy question which maps each commits to its SHA. */
  def dummy_question(input: DataStream[Commit]): DataStream[String] = {
    input.map(_.sha)
  }

  /**
   * Write a Flink application which outputs the sha of commits with at least 20 additions.
   * Output format: sha
   */
  def question_one(input: DataStream[Commit]): DataStream[String] = {
    input.filter(x => x.stats.isDefined).filter(x => x.stats.get.additions >= 20).map(x => x.sha)
  }

  /**
   * Write a Flink application which outputs the names of the files with more than 30 deletions.
   * Output format:  fileName
   */
  def question_two(input: DataStream[Commit]): DataStream[String] = {
    input.flatMap(x => x.files).filter(x => x.filename.isDefined).filter(x => x.deletions > 30).map(x => x.filename.get)
  }

  /**
   * Count the occurrences of Java and Scala files. I.e. files ending with either .scala or .java.
   * Output format: (fileExtension, #occurrences)
   */
  def question_three(input: DataStream[Commit]): DataStream[(String, Int)] = {
    input.flatMap(x => x.files).filter(x => x.filename.isDefined).map(x => x.filename.get).map(x => {
      val temp = x.split("\\.").last
      temp match {
        case "java" => ("java", 1)
        case "scala" => ("scala", 1)
        case _ => ("other", 1)

      }
    }).filter(x => x._1.equals("java") || x._1.equals("scala")).keyBy(_._1)
      .sum(1)

  }

  /**
   * Count the total amount of changes for each file status (e.g. modified, removed or added) for the following extensions: .js and .py.
   * Output format: (extension, status, count)
   */
  def question_four(
                     input: DataStream[Commit]): DataStream[(String, String, Int)] = {
    input.flatMap(x => x.files)
      .filter(x => x.filename.isDefined)
      .filter(x => x.status.isDefined)
      .map(x => (x.filename.get, x.status.get, x))
      .map(x => {
        val temp = x._1.split("\\.").last
        temp match {
          case "js" => (("js", x._2), x._3.changes)
          case "py" => (("py", x._2), x._3.changes)
          case _ => (("other", x._2), x._3.changes)

        }
      }).filter(x => x._1._1.equals("js") || x._1._1.equals("py")).filter(x => x._2 > 0).keyBy(_._1).sum(1)
      .map(x => (x._1._1, x._1._2, x._2))


  }

  def getTimeString(date: Date): String = {
    val dateFormat = new SimpleDateFormat(
      "dd-MM-yyyy")


    dateFormat.format(date)


  }

  /**
   * For every day output the amount of commits. Include the timestamp in the following format dd-MM-yyyy; e.g. (26-06-2019, 4) meaning on the 26th of June 2019 there were 4 commits.
   * Make use of a non-keyed window.
   * Output format: (date, count)
   */
  def question_five(input: DataStream[Commit]): DataStream[(String, Int)] = {


    input
      .assignAscendingTimestamps(x => x.commit.committer.date.getTime)
      .map(x => (getTimeString(x.commit.committer.date), 1))
      .windowAll(TumblingEventTimeWindows.of(Time.days(1)))
      .sum(1)


  }

  /**
   * Consider two types of commits; small commits and large commits whereas small: 0 <= x <= 20 and large: x > 20 where x = total amount of changes.
   * Compute every 12 hours the amount of small and large commits in the last 48 hours.
   * Output format: (type, count)
   */
  def question_six(input: DataStream[Commit]): DataStream[(String, Int)] = {
    input
      .filter(x => x.stats.isDefined)
      .assignAscendingTimestamps(x => x.commit.committer.date.getTime)
      .map(x => {
        if (0 <= x.stats.get.total && x.stats.get.total <= 20)
          ("small", 1)
        else
          ("large", 1)

      })
      .keyBy(_._1)
      .window(SlidingEventTimeWindows.of(Time.hours(48), Time.hours(12)))
      .sum(1)
  }

  class MyProcessWindowFunction extends ProcessWindowFunction[(String, Commit), CommitSummary, String, TimeWindow] {

    def process(key: String, context: Context, input: Iterable[(String, Commit)], out: Collector[CommitSummary]) = {
      val date = getTimeString(input.toList.apply(0)._2.commit.committer.date)

      val commitsNr = input.toList.size

      val commitersNr = input.map(x => x._2.commit.committer.name).toList.distinct.size

      val totalChanges = input.filter(x => x._2.stats.isDefined).map(x => x._2.stats.get.total).reduce((a, b) => a + b)

      val maxCommits = input.map(x => (x._2.commit.committer.name, 1)).groupBy(_._1).map(y => (y._1, y._2.size))
        .maxBy(_._2)._2
      
      val maxComittersNames = input.map(x => (x._2.commit.committer.name, 1))
        .groupBy(_._1).map(y => (y._1, y._2.size)).filter(z => z._2 == maxCommits)
        .map(x => x._1).toList.sorted
        .reduce((a, b) => a + "," + b)


      out.collect(CommitSummary(key, date, commitsNr, commitersNr, totalChanges, maxComittersNames))
    }
  }

  /**
   * For each repository compute a daily commit summary and output the summaries with more than 20 commits and at most 2 unique committers. The CommitSummary case class is already defined.
   *
   * The fields of this case class:
   *
   * repo: name of the repo.
   * date: use the start of the window in format "dd-MM-yyyy".
   * amountOfCommits: the number of commits on that day for that repository.
   * amountOfCommitters: the amount of unique committers contributing to the repository.
   * totalChanges: the sum of total changes in all commits.
   * topCommitter: the top committer of that day i.e. with the most commits. Note: if there are multiple top committers; create a comma separated string sorted alphabetically e.g. `georgios,jeroen,wouter`
   *
   * Hint: Write your own ProcessWindowFunction.
   * Output format: CommitSummary
   */
  def question_seven(commitStream: DataStream[Commit]): DataStream[CommitSummary] = {

    commitStream
      .map(x => (x.url.split("/")(4) + "/" + x.url.split("/")(5), x))
      .assignAscendingTimestamps(x => x._2.commit.committer.date.getTime)
      .keyBy(x => x._1)
      .timeWindow(Time.hours(24))
      .process(new MyProcessWindowFunction())
      .filter(y => y.amountOfCommits > 20 && y.amountOfCommitters <= 2)

  }


}
