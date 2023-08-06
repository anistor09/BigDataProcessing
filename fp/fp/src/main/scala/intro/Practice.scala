package intro

/**
  * This part has some exercises for you to practice with the recursive lists and functions.
  * For the exercises in this part you are _not_ allowed to use library functions,
  * you should implement everything yourself.
  * Use recursion to process lists, iteration is not allowed.
  *
  * This part is worth 16 points.
  */
object Practice {

    /** Q10 (2p)
      * Implement the function that returns the first `n` elements from the list.
      * Note that `n` is an upper bound, the list might not have `n` elements.
      *
      * @param xs list to take items from.
      * @param n amount of items to take.
      * @return the first n items of xs.
      */
    def firstN(xs: List[Int], n: Int): List[Int] = xs match{

        case i :: tail => if(n>0) i :: firstN(tail,n-1)
        else Nil

        case Nil=> Nil
    }


    /** Q11 (4p)
      * Implement the function that returns the maximum value in the list.
      * If the list is empty, return `Int.MinValue`
      *
      * @param xs list to process.
      * @return the maximum value in the list.
      */
    def maxValue(xs: List[Int]): Int = xs match {

        case Nil => Int.MinValue
        case i:: tail => maxValueHelper(tail,i)
    }

    def maxValueHelper(xs: List[Int], max : Int): Int = xs match {
        case i :: tail => if(i>max)
            maxValueHelper(tail,i)
        else
            maxValueHelper(tail, max)
        case Nil => max
    }

    /** Q12 (3p)
     * given two Ints, generate the List[Int] with both numbers inclusive
     *
     * Examples:
     * intList(2,7) // List(2,3,4,5,6,7)
     * intList(3,0) // List()
     */
    def intList(a: Int, b: Int) : List[Int] = {
        if(b<a)
            return List()
        else
            return intListHelper(a,b,List())
    }

    def intListHelper(a: Int, b: Int, xs: List[Int]) : List[Int] = {
        if(b<a)
            return xs;
        else
            return intListHelper(a,b-1,b::xs)
    }



    /**
     * Q13 (7p)
     * This question is a variant on a filter function. Given a List[A] and a
     * function f: A => Boolean, `myFilter` should retain all elements from
     * the list which satisfy 'f' and throw out all other elements, but...
     * ... it has a twist: It should also throw out each even indexed list
     * element which satisfy 'f'
     *
     * Take a look at the examples to see more directly what it needs to do
     * if you find this description vague.
     *
     * You are required to solve this using pattern matching on lists.
     * HINT: define a 'helper' method within myFilter which uses case matching.
     *
     * Examples:
     * 	val nrs = List.range(0,11) // List(0,1,2,3,...,10)
     * 	myFilter(nrs, (i: Int) => i % 2 == 0) // List(0,4,8)
     *
     * so although 2, 6 and 10 satisfy the function, they are thrown out.
     */
    // a helper method which you've written yourself
    def myFilter[A](xs: List[A], f: A => Boolean) : List[A] = myFilterHelper(xs, f, true)

    def myFilterHelper[A](xs: List[A], f: A => Boolean, add: Boolean) : List[A] = xs match{


        case i:: tail => if(f(i)) {
                            if(add)
                            i::myFilterHelper(tail,f,false)
                            else myFilterHelper(tail,f,true)
        } else myFilterHelper(tail,f,add)
        case Nil => Nil
    }
}
