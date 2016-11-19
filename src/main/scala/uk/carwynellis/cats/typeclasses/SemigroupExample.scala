package uk.carwynellis.cats.typeclasses

import cats._
import cats.implicits._

/**
  * A semigroup for some type A has a single binary operation returning a value
  * of type A.
  *
  * The binary operation, called combine here, must be associative. That is
  *
  * ( ( a combine b ) combine c ) == ( a combine ( b combine c ) )
  *
  * For example, multiplication and addition are associative, whereas
  * subtraction is not.
  *
  * See - http://typelevel.org/cats/typeclasses/semigroup.html
  */
object SemigroupExample extends App {

  assert(Semigroup[Int].combine(1,2) == 3)

  assert(
    Semigroup[List[Int]].combine(List(1,2,3), List(4,5,6)) == List(1,2,3,4,5,6)
  )

  assert(Semigroup[Option[Int]].combine(Option(1), Option(2)) == Option(3))

  assert(
    Semigroup[Int => Int].combine(
      {(x: Int) => x + 1},
      {(x: Int) => x * 10}
    ).apply(6) == 67
  )

  // Many types have existing methods for combining instances of that type,
  // however with semigroup the combine function composes.

  // Consider combining two maps that share a (K, V) pair.

  // Combining via semigroup yields
  assert(
      Map("foo" -> 3).combine(Map("foo" -> 4)) == Map("foo" -> 7)
  )

  // Whereas ++ simply replaces the value in the first map with that in the
  // second map.
  assert(Map("foo" -> 3) ++ Map("foo" -> 4) == Map("foo" -> 4))

  // This also holds for nested structures containing associative types...
  assert {
    val combined = Map("foo" -> Map("bar" -> 5))
      .combine(Map("foo" -> Map("bar" -> 6), "baz" -> Map()))

    combined == Map(
      "foo" -> Map("bar" -> 11),
      "baz" -> Map()
    )
  }

  // ...which compares to the existing Map ++ operator
  assert {
    val combined = Map("foo" -> Map("bar" -> 5)) ++
      Map("foo" -> Map("bar" -> 6), "baz" -> Map())

    combined == Map(
      "foo" -> Map("bar" -> 6),
      "baz" -> Map()
    )
  }

  // An inline syntax, following the scalaz convention, using the |+| operator
  // is also available.
  assert( (Option(1) |+| Option(2)) == Some(3))

  assert( (List(1,2,3) |+| List(4,5,6)) == List(1,2,3,4,5,6))

  // Note - when working with Options, cats only defines type classes on Option
  //        so using values declared as Some or None will not work.
  //
  //        For example Some(1) |+| None will fail to compile.
}
