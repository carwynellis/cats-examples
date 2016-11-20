package uk.carwynellis.cats.typeclasses.functors

import cats._
import cats.data.Nested

import language.postfixOps

/**
  * Apply extends the Functor typeclass with a new function, ap.
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
}
