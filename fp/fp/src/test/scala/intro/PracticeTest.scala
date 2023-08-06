package intro

import Practice._
import org.scalatest.FunSuite

class PracticeTest extends FunSuite {

    test("FirstN") {
        assertResult((1 to 10).toList) {
            firstN((1 to 20).toList, 10)
        }
    }

    test("MaxValue") {
        assertResult(16) {
            maxValue(List(10, 4, 14, -4, 15, 14, 16, 7))
        }
    }

    test("intLIst") {
        assertResult(List()) {
            intList(3,0)
        }
    }

    test("myFilter") {
        assertResult(List(0,4,8) ){

            	myFilter(List.range(0,11), (i: Int) => i % 2 == 0)
        }
    }
}
