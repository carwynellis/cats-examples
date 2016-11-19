package uk.carwynellis.cats.typeclasses

import cats._
import cats.implicits._

/**
  * Monoid extends Semigroup, adding an empty method, that returns a value that
  * can be combined with other values without modifying the other value.
  *
  * For example Monoid[String] may define empty as "", Monoid[Int] 0 etc...
  *
  * See http://typelevel.org/cats/typeclasses/monoid.html
  */
object MonoidExample extends App {

  // Examples of empty values
  assert(Monoid[String].empty == "")
  assert(Monoid[Int].empty == 0)
  assert(Monoid[Option[Int]].empty == None)

  // The empty value provides a default value allowing us to combine elements
  // of a collection that could be empty. If the collection is empty then we
  // can use the empty value as a fallback rather.
  assert(Monoid[String].combineAll(List("a", "b", "c")) == "abc")
  assert(Monoid[String].combineAll(List.empty[String]) == "")

  // Again, the advantage here is composition. Monoids can be composed to
  // operate on more complex types.
  assert {
    val combined = Monoid[Map[String,Int]].combineAll(List(
      Map("a" -> 1, "b" -> 2),
      Map("a" -> 3)
    ))

    combined == Map("a" -> 4, "b" -> 2)
  }

  assert(Monoid[Map[String,Int]].combineAll(List.empty) == Map.empty)

  // Monoids can also be used to combine values of a given type, where a
  // monoid exists for the type, for example by using foldMap from Foldable,
  // which combines results using the monoid available for the type being
  // mapped over.
  val l = List(1,2,3,4,5)

  assert(l.foldMap(identity) == 15)
  assert(l.foldMap(i => i.toString) == "12345")
  // Cats also provides support for a mapping function that returns a tuple.
  assert(l.foldMap(i => (i, i.toString)) == (15, "12345"))
}
