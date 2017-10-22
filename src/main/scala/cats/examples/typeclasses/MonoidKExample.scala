package cats.examples.typeclasses

import cats._
import cats.implicits._

/**
  * MonoidK is a universal monoid which operates on kinds.
  *
  * This type is useful when its type parameter F[_] has a structure that can
  * be combined for any particular type, and which also has an empty
  * representation. Thus MonoidK is like a Monoid for kinds or parameterised
  * types.
  *
  * A MonoidK[F] can produce a Monoid[F[A]] for any type A.
  *
  * Here's how to distinguish Monoid and MonoidK
  *
  * - Monoid[A] allows A values to be combined and also means there is an empty
  *   A value that functions as an identity
  *
  * - MonoidK[F] allows two F[A] values to be combined for any A. It also means
  *   that for any A there is an empty F[A] value. The combination operation
  *   and empty value depend on the structure of F, but not on the structure of
  *   A
  *
  * See http://typelevel.org/cats/typeclasses/monoidk.html
  */
object MonoidKExample extends App {

  // Lets compare the usage of Monoid[A] and MonoidK[F]

  // Just like Monoid[A], MonoidK[F] has an empty method, but is parameterised
  // on the type of element contained in F.

  assert(Monoid[List[String]].empty == List.empty[String])

  assert(MonoidK[List].empty[String] == List.empty[String])

  assert(MonoidK[List].empty[Int] == List.empty[Int])

  // And instead of combine it has combineK which takes an additional type
  // parameter.
  assert(Monoid[List[Int]].combine(List(1,2), List(3,4)) == List(1,2,3,4))

  assert(MonoidK[List].combineK[Int](List(1,2), List(3,4)) == List(1,2,3,4))

  assert(
    MonoidK[List].combineK[String](List("a", "b"), List("c", "d"))
      == List("a", "b", "c", "d")
  )

  // In most cases scala is able to infer the type so the explicit type
  // parameter can be ommitted.
  assert(MonoidK[List].combineK(List(1,2), List(3,4)) == List(1,2,3,4))

  assert(
    MonoidK[List].combineK(List("a", "b"), List("c", "d"))
      == List("a", "b", "c", "d")
  )

  // MonoidK extends SemigroupK so refer to the SemigroupK documentation for
  // futher examples.
}
