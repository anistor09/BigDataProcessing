package intro

import org.scalatest.FunSuite
import intro.PatternMatching2._

class PatternMatching2Test extends FunSuite{


  test(" twice") {
    assertResult(List("abc", "abc")) {
      twice(List("abc"))
    }
  }

  test("drunkWords") {
    assertResult(List("uoy","yeh")) {
      drunkWords(List("hey", "you"))
    }
  }

  test("lastElem") {
    assertResult(Some("yes")) {
      lastElem(List("no", "yes", "no", "no", "yes"))
    }
  }
  test("myForAll") {
    val startsWithS = (s: String) => s.startsWith("s") // lambda expression

     					// false
     // 	// true
    assertResult(true) {
      myForAll(List("start", "strong", "system"), startsWithS)
    }
  }
  test("myForAll2") {
    val startsWithS = (s: String) => s.startsWith("s") // lambda expression

    // false
    // 	// true
    assertResult(false) {
      myForAll(List("tart", "rong", "ystem"), startsWithS)
    }
  }

  test("append") {
    assertResult(List(1,3,5,2,4)) {
      append(List(1,3,5), List(2,4))
    }
  }
}
