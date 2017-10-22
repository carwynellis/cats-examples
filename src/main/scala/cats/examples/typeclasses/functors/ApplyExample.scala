package cats.examples.typeclasses.functors

import cats._
import cats.data.Nested
import cats.syntax.cartesian._

import language.postfixOps

/**
  * Apply extends the Functor type class with a new function, ap.
  *
  * ap is similar to map, in that we are transforming a value within a context
  * such as Option, List, i.e. the F in F[A].
  *
  * ap differs from map in the type of transformation, which is F[A => B]
  * instead of A => B for map.
  *
  * See http://typelevel.org/cats/typeclasses/apply.html
  */
object ApplyExample extends App {

  // Some simple functions
  val intToString: Int => String = _.toString
  val double: Int => Int = _ * 2
  val addTwo: Int => Int = _ + 2

  // Apply provides ap and map methods which must be implemented.
  implicit val optionApply: Apply[Option] = new Apply[Option] {
    def ap[A, B](f: Option[A => B])(fa: Option[A]): Option[B] =
      fa.flatMap (a => f.map (ff => ff(a)))

    def map[A,B](fa: Option[A])(f: A => B): Option[B] = fa map f
  }

  implicit val listApply: Apply[List] = new Apply[List] {
    def ap[A, B](f: List[A => B])(fa: List[A]): List[B] =
      fa.flatMap (a => f.map (ff => ff(a)))

    def map[A,B](fa: List[A])(f: A => B): List[B] = fa map f
  }

  // Apply extends Functor so we can map.
  assert(Apply[Option].map(Some(1))(intToString) contains "1" )

  assert(Apply[Option].map(Some(1))(double) contains 2 )

  assert(Apply[Option].map(None)(double) isEmpty)

  // Like Functors, Apply instances also compose, via the Nested type.
  val listOption = Nested[List, Option, Int](List(Some(1), None, Some(3)))

  val plusOne = (x: Int) => x + 1

  val f = Nested[List, Option, Int => Int](List(Some(plusOne)))

  assert(Apply[Nested[List, Option, ?]].ap(f)(listOption)
    == Nested[List, Option, Int](List(Some(2), None, Some(4))))

  // In addition to map from Functor, Apply provides the ap function.
  // Compare the ap invocation below with that of map above.
  // This highlights the difference in types between ap and map.
  // ap describes a transformation of F[A => B] hence below, we define
  // Some(function) which is applied to an option.
  assert(Apply[Option].ap(Some(intToString))(Some(1)) contains "1")

  assert(Apply[Option].ap(Some(double))(Some(1)) contains 2)

  assert(Apply[Option].ap(Some(double))(None) isEmpty)

  assert(Apply[Option].ap(None)(Some(1)) isEmpty)

  assert(Apply[Option].ap(None)(None) isEmpty)

  // Apply also provides variants of ap, ap2 to ap22 that support additional
  // arguments. Note that ap just supports a single argument.
  val addArity2 = (a: Int, b: Int) => a + b

  assert(Apply[Option].ap2(Some(addArity2))(Some(1), Some(2)) contains 3)

  val addArity3 = (a: Int, b: Int, c: Int) => a + b + c

  assert(Apply[Option].ap3(Some(addArity3))(Some(1), Some(2), Some(3)) contains 6)

  // ap22 left as an exercise for the reader...

  // Note that if any of the arguments in the examples above are None, the
  // result of the computation will be None too. The effects of the context we
  // are operating on are applied to the entire computiation.
  assert(Apply[Option].ap2(Some(addArity2))(Some(1), None) isEmpty)

  assert(Apply[Option].ap2(None)(Some(1), Some(2)) isEmpty)

  // Similarly map2..22 methods are provided...
  assert(Apply[Option].map2(Some(1), Some(2))(addArity2) contains 3)

  assert(Apply[Option].map3(Some(1), Some(2), Some(3))(addArity3) contains 6)

  // ...along with tupleN.
  assert(Apply[Option].tuple2(Some(1), Some(2)) contains (1,2))

  assert(Apply[Option].tuple3(Some(1), Some(2), Some(3)) contains (1,2,3))

  // Apply also provides a builder syntax, for higher arity functions, via the
  // |@| operator. This relies on importing cats.syntax.all._ or
  // cats.syntax.cartesian._
  // The following example compares the builder syntax with using a specific
  // arity function, in this case map3.

  // f1 is a function with arity 3...
  def f1(a: Option[Int], b: Option[Int], c: Option[Int]) =
    (a |@| b |@| c) map { _ * _ * _ }
  // which is equivalent to invoking map3 directly...
  def f2(a: Option[Int], b: Option[Int], c: Option[Int]) =
    Apply[Option].map3(a, b, c) { _ * _ * _ }

  assert(f1(Some(1), Some(2), Some(3)) == f2(Some(1), Some(2), Some(3)))

  // All instances created using |@| have ap, map and tupled methods of the
  // appropriate arity. For example f1 above has functions of arity 3.
}
