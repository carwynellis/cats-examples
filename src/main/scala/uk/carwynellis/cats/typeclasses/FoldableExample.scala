package uk.carwynellis.cats.typeclasses

import cats._
import cats.implicits._

import scala.language.postfixOps

/**
  * Foldable type classes can be defined for data structures that can be folded
  * to a summary value.
  *
  * In the case of a collection, such as a list or set, these methods will
  * fold together, or combine, the values contained in the collection to
  * produce a single result.
  *
  * Most collection types have a foldLeft method which will be used by the
  * associated Foldable[_] instance.
  *
  * Foldable[F] is implemented in terms of two fold methods
  *
  *  - foldLeft(fa, b)(f) eagerly folds fa from left-to-right.
  *  - foldRight(fa, b)(f) lazily folds fa from right-to-left.
  *
  * See http://typelevel.org/cats/typeclasses/foldable.html
  */
object FoldableExample extends App {

  // Foldable provides a number of methods which are shown in the examples
  // below.

  assert(Foldable[List].fold(List("a", "b", "c")) == "abc")

  assert(Foldable[List].foldMap(List(1,2,3))(_.toString) == "123")

  assert(
    Foldable[List].foldK(List(List(1,2,3), List(4,5,6)))
      == List(1,2,3,4,5,6)
  )

  assert(
    Foldable[List].reduceLeftToOption(List.empty[Int])(_.toString) { (s,i) =>
      s + i
    } isEmpty
  )

  assert(
    Foldable[List].reduceLeftToOption(List(1,2,3))(_.toString) { (s, i) =>
      s + i
    } contains "123"
  )

  assert(
    Foldable[List].reduceRightToOption(List.empty[Int])(_.toString) { (i,s) =>
      Later(s.value + i)
    }.value isEmpty
  )

  assert(
    Foldable[List].reduceRightToOption(List(1,2,3))(_.toString) { (i,s) =>
      Later(s.value + i)
    }.value contains "321"
  )

  assert(Foldable[Set].find(Set(1,2,3))(_ > 2) contains 3)

  assert(Foldable[Set].exists(Set(1,2,3))(_ > 2))

  assert(! Foldable[Set].forall(Set(1,2,3))(_ > 2))

  assert(Foldable[Set].forall(Set(1,2,3))(_ < 4))

  assert(Foldable[Vector].filter_(Vector(1,2,3))(_ < 3) == List(1,2))

  assert(! Foldable[List].isEmpty(List(1,2,3)))

  assert(Foldable[List]isEmpty(List.empty[Int]))

  assert(Foldable[List].nonEmpty(List(1,2,3)))

  assert(Foldable[Option].toList(Option(1)) == List(1))

  assert(Foldable[Option].toList(None) == List.empty)
}
