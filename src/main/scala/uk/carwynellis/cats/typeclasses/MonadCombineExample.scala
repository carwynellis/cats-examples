package uk.carwynellis.cats.typeclasses

import cats._
import cats.implicits._

/**
  * The MonadCombine typeclass is a combination of a Monad with a MonoidK and
  * provides the following methods
  *
  *   unite - given an F[G[A]] folds over the inner G[A] accumulating the
  *           'interesting' values, resulting in an F[A]
  *   separate - takes an F[G[A,B]] separating the left and right values into
  *              an F[A] and F[B] respectively
  */
object MonadCombineExample extends App {

  assert(MonadCombine[List].unite(List(Option(1), Option(2), None, Option(3))) == List(1,2,3))

  assert(MonadCombine[List].separate(List(("A", 1), ("B", 2))) == ( List("A","B"), List(1,2)))
}
