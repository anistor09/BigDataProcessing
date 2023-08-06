package DataFrameAssignment

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col

/**
 * Please read the comments carefully, as they describe the expected result and may contain hints in how
 * to tackle the exercises. Note that the data that is given in the examples in the comments does
 * reflect the format of the data, but not the result the graders expect (unless stated otherwise).
 */
object DFAssignment {

  /**
   * In this exercise we want to know all the commit SHA's from a list of committers. We require these to be
   * ordered according to their timestamps in the following format:
   *
   * | committer      | sha                                      | timestamp            |
   * |----------------|------------------------------------------|----------------------|
   * | Harbar-Inbound | 1d8e15a834a2157fe7af04421c42a893e8a1f23a | 2019-03-10T15:24:16Z |
   * | ...            | ...                                      | ...                  |
   *
   * Hint: Try to work out the individual stages of the exercises. This makes it easier to track bugs, and figure out
   * how Spark DataFrames and their operations work. You can also use the `printSchema()` function and `show()`
   * function to take a look at the structure and contents of the DataFrames.
   *
   * @param commits Commit DataFrame, see commit.json and data_raw.json for the structure of the file, or run
   *                `println(commits.schema)`.
   * @param authors Sequence of Strings representing the authors from which we want to know their respective commit
   *                SHA's.
   * @return DataFrame of commits from the requested authors, including the commit SHA and the according timestamp.
   */
  def assignment_12(commits: DataFrame, authors: Seq[String]): DataFrame = {

    val result = commits
      .select("commit.committer.name", "sha", "commit.author.date")

      .filter(col("commit.committer.name").isin(authors: _*))
      .orderBy("commit.author.date")

    result

  }

}
