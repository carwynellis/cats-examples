package uk.carwynellis.cats.typeclasses.functors

import cats._
import cats.implicits._
import cats.data.Nested

/**
  * Applicative extends Apply adding a single method pure, which takes any
  * value and returns it in the context of the Functor. For example, for an
  * Option, the pure function wraps the value with a Some().
  *
  * See http://typelevel.org/cats/typeclasses/applicative.html
  */
object ApplicativeExample extends App {

  // Example application of pure for Option and List.
  assert(Applicative[Option].pure(1) contains 1)
  assert(Applicative[List].pure(2) == List(2))

  // Like Functor and Apply, Applicative functors also compose naturally with
  // each other.

  // For example using Nested again we can combine List and Option as follows
  assert(Applicative[Nested[List, Option, ?]].pure(1) == Nested(List(Some(1))))

}
