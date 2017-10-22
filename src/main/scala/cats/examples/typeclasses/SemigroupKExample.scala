package cats.examples.typeclasses

import cats._
import cats.implicits._

/**
  * SemigroupK has a very similar structure to Semigroup, the difference being
  * that SemigroupK operates on type constructors of one argument. So for
  * example, whereas you can find a Semigroup for types that are fully
  * specified, such as Int or List[Int], you will find a SemigroupK for List
  * and Option.
  *
  * These types are type constructors in the sense that you can think of them
  * as 'functions' in the type space.
  *
  * You can think of the List type as a function which takes a concrete type
  * such as Int, and returns a concrete type List[Int].
  *
  * This can also be referred to as having kind: * -> *, so for instance Int
  * would have kind * and map, *,* -> *.
  *
  * In fact the K in SemigroupK stands for K.
  *
  * See http://typelevel.org/cats/typeclasses/semigroupk.html
  */
object SemigroupKExample extends App {

  // For Lists, combine from Semigroup, and combineK from SemigroupK are both
  // list concatenation.
  assert(Semigroup[List[Int]].combine(List(1,2), List(3,4)) == List(1,2,3,4))

  assert(SemigroupK[List].combineK[Int](List(1,2), List(3,4)) == List(1,2,3,4))

  // However for Option, combine and combineK differ.

  // Since Semigroup operates on fully specified types, a Semigroup[Option[A]]
  // knows the concrete type of A and will use Semigroup[A].combine to combine
  // the As. Consequently an implicit Semigroup[A] is required.

  // In contrast, since SemigroupK operates on Option, where the inner type is
  // not fully specified, we cannot know how to combine them. Therefore in the
  // case of Option, combineK must use the orElse method of Option instead.

  assert(Semigroup[Option[Int]].combine(Some(1), Some(2)) contains 3)

  assert(SemigroupK[Option].combineK[Int](Some(1), Some(2)) contains 1)

  assert(SemigroupK[Option].combineK[Int](Some(1), None) contains 1)

  assert(SemigroupK[Option].combineK[Int](None, Some(2)) contains 2)

  // Inline syntax is also available for Semigroup and SemigroupK, following
  // the convention from scalaz that |+| represents combine, and <+> combineK.

  val one = Option(1)
  val two = Option(2)
  // Note that we must declare the type explicitly as Option since SemigroupK
  // instances exist for Option only, not Some and None.
  val n: Option[Int] = None

  assert(one |+| two contains 3)

  assert(one <+> two contains 1)

  assert(n |+| two contains 2)

  assert(n <+> two contains 2)
}
