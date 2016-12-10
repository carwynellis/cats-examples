package uk.carwynellis.cats.typeclasses

import cats._
import cats.implicits._

/**
  * The show type class provides textual representations and is intended to be
  * a better 'toString'.
  *
  * Whereas toString exists for any Object, a Show instance will only exist if
  * one has been explicitly provided.
  */
object ShowExample extends App {

  assert(Show[Int].show(1) == "1")

  assert(Show[Option[Int]].show(Option(2)) == "Some(2)")

}
