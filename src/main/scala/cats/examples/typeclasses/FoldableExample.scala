package cats.examples.typeclasses

import cats._
import cats.implicits._
import cats.data.Nested

import scala.language.postfixOps
import scala.util.Try

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

  // The foldLeft and foldRight methods form the basis for many useful
  // operations as shown in the examples below.

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

  def parseInt(s: String): Option[Int] = Try(Integer.parseInt(s)).toOption

  // traverse_ traverse F[A] using Applicative[G]. A values will be mapped into
  // G[B] and combined using map2 from Applicative. Primarily useful where the
  // side-effect of G[_] is needed since the result is discarded and G[Unit] is
  // returned.
  // So in the following example the result of parseInt is discarded and
  // success indicated by the result, some unit.
  assert(Foldable[List].traverse_(List("1", "2"))(parseInt) contains (()))

  // If parsing fails then this results in None being returned.
  assert(Foldable[List].traverse_(List("1", "A"))(parseInt) isEmpty)

  // sequence_ behaves in a similar manner when sequencing over nested types.
  assert(Foldable[List].sequence_(List(Option(1), Option(2))) contains (()))

  assert(Foldable[List].sequence_(List(Option(1), None)) isEmpty)

  assert(Foldable[List].dropWhile_(List(1,2,3,4,5))(_ < 4) == List(4,5))

  // Examples of folding over nested types.
  val listOption1 = Nested(List(Option(1), Option(2), Option(3)))
  val listOption2 = Nested(List(Option(1), Option(2), None))

  assert(Foldable[Nested[List, Option, ?]].fold(listOption1) == 6)

  assert(Foldable[Nested[List, Option, ?]].fold(listOption2) == 3)

  // Thus when defining some new data structure, if we can define foldLeft and
  // foldRight methods, we are able to provide many other useful operations,
  // if not the most efficient implementations, without further work.

  // Note that in order to support laziness, the signature of Foldable's
  // foldRight is
  //
  // def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B]
  //
  // as opposed to
  //
  // def foldRight[A, B](fa: F[A], z: B)(f: (A, B) => B): B
  //
  // which someone familiar with foldRight from collections in the scala
  // standard library might expect.
  //
  // This prevents structures which are lazy in their right hand argument from
  // being eagerly evaluated.

  // For example consider the following infinite stream of false values

  val allFalse = Stream.continually(false)

  // If we wanted to reduce this stream into a single false value using the
  // logical &&, we know that we do not need to consider the entire stream to
  // arrive at a false value.

  // Using the foldRight method provided by the standard library will attempt
  // to evaluate the entire stream, resulting in a stack overflow.

  try {
    allFalse.foldRight(true)(_ && _)
  }
  catch {
    case e: StackOverflowError => println(s"Caught expected stack overflow")
  }

  // However with the lazy foldRight from Foldable evaluation will stop after
  // examining only the first value.

  assert(
    Foldable[Stream].foldRight(allFalse, Eval.True) { (a,b) =>
      if (a) b
      else Eval.now(false)
    }.value == false
  )
}
