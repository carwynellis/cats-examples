package uk.carwynellis.cats.typeclasses.functors

import cats._
import cats.data.Nested
import cats.implicits._

/**
  * A functor is a type class involving types that have one 'hole', such as
  * Option or List, in contrast to say Int which as no 'holes'.
  *
  * Learn you a Haskell describes Functor as a type class 'for things that can
  * be mapped over'.
  *
  * Functors provide a single operation, map which takes a function A => B
  * and turns an F[A] into an F[B].
  *
  * See
  *
  *   http://typelevel.org/cats/typeclasses/functor.html
  *   http://learnyouahaskell.com/making-our-own-types-and-typeclasses#the-functor-typeclass
  *
  */
object FunctorExample extends App {

  // Functors can be created for existing types that already provide a map
  // method.
  implicit val optionFunctor: Functor[Option] = new Functor[Option] {
    override def map[A,B](fa: Option[A])(f: A => B) = fa map f
  }

  implicit val listFunctor: Functor[List] = new Functor[List] {
    override def map[A,B](fa: List[A])(f: A => B) = fa map f
  }

  // Functors can also be created for types which do not have a map method.
  // For example we can create a Functor for Function[In, ?] and implement map
  // using andThen.
  // Note - that this relies on the kind projector plugin which cleans up the
  //        syntax when the number of type 'holes' are changed. For example
  //        here we are changing Function[?,?] into Function[In,?]
  // See - https://github.com/non/kind-projector
  //
  // Also more detailed treatment of functors can be found in learn you a haskell
  // at http://learnyouahaskell.com/functors-applicative-functors-and-monoids
  implicit def function1Functor[In]: Functor[Function1[In, ?]] =
    new Functor[Function1[In, ?]] {
      def map[A,B](fa: In => A)(f: A => B): Function1[In,B] = fa andThen f
    }

  // Using functor.

  // List is a Functor that applies the function to each element of the list.
  assert(Functor[List].map(List("a", "foo"))(_.length) == List(1,3))

  // Option is also a functor.
  assert(Functor[Option].map(Some("foo"))(_.length) == Some(3))

  // In the None case the function is not applied and None is returned.
  assert(Functor[Option].map(None) { s: String =>
    println("This will not be executed")
    s.length
  } == None)

  // Mapping over Function1.
  def intToString(i: Int): String = s"$i"
  def stringLength(s: String): Int = s.length

  assert(Functor[Function1[Int, ?]].map(intToString)(stringLength)(12) == 2)

  // Which is equivalent to...
  assert( (intToString _ andThen stringLength _)(12) == 2)

  // Functor can be used to lift a function from A => B to F[A] => F[B].
  val stringLengthOption: Option[String] => Option[Int] = Functor[Option].lift(stringLength)

  assert( stringLengthOption(Some("foo")) == Some(3))

  // Functor also provides an fproduct function which pairs a value to the
  // result of applying that value to function.
  val strings = List("a", "bb", "ccc")
  assert(Functor[List].fproduct(strings)(stringLength).toMap == Map(
    "a" -> 1,
    "bb" -> 2,
    "ccc" -> 3
  ))

  // Functors can be composed. Given two functors F[_] and G[_] a new functor
  // F[G[_]] can be created using the Nested data type.
  val listOption = Nested[List, Option, Int](List(Some(1), None, Some(3)))
  assert(
    // TODO - why is the third type parameter ? here
    Functor[Nested[List, Option, ?]].map(listOption)(_ + 1)
      == Nested[List, Option, Int](List(Some(2), None, Some(4)))
  )

  val optionList = Nested[Option, List, Int](Some(List(1,2,3)))
  assert(
    // TODO - why is the third type parameter ? here
    Functor[Nested[Option, List, ?]].map(optionList)(_ + 1)
      == Nested[Option, List, Int](Some(List(2,3,4)))
  )

  // Functors have a natural relationship with subtyping.

  // Given a simple class hierarchy
  class A
  class B extends A

  // An instance of B can also be an instance of A.
  val b = new B
  val a: A = b

  // It follows that a List[B] can be mapped into a List[A].
  val listOfA1: List[A] = List(new B).map(b => b: A)
  val listOfA2: List[A] = List(new B).map(identity[A])
  // Functor provides widen as a convenience.
  val listOfA3: List[A] = Functor[List].widen[B,A](List(new B))
}
