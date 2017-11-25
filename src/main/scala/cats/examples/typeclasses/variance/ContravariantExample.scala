package cats.examples.typeclasses.variance

import cats._
import cats.implicits._

import scala.math.Ordered._

/**
  * The Contravariant type class is for functors that define a contramap with
  * the following type
  *
  *   def contramap[A,B](fa: F[A])(f: B => A): F[B]
  *
  * It looks like a covariant map, but with the transformation f: A => B
  * reversed.
  *
  * See http://typelevel.org/cats/typeclasses/contravariant.html
  */
object ContravariantExample extends App {

  // Generally speaking, if you have some context F[A] for type A, you can get
  // an A value out of a B value. Contravariant allows you to get the F[B]
  // context for B.

  // Examples of Contravariant instances are Show and scala.math.Ordering
  // (along with cats.kernel.Order).

  // Suppose we have Money and Salary classes with a Show instance for Money.

  case class Money(amount: Int)
  case class Salary(size: Money)

  implicit val showMoney: Show[Money] = Show.show(m => s"£${m.amount}")

  // If we want to show a Salary instance, we can just convert it to a Money
  // instance and show that instead.

  implicit val showSalary: Show[Salary] = showMoney.contramap(_.size)

  assert(Salary(Money(1000)).show == "£1000")

  // The Show example above is a little trivial and contrived.

  // Contravariant can also help wth orderings.

  // The scala.math.Ordering type class defines comparison operatores, such as
  // compare.
  assert(Ordering.Int.compare(2,1) == 1)
  assert(Ordering.Int.compare(1,2) == -1)

  // There is also a method by, that creates a new ordering from existing ones.

  def by[T, S](f: T => S)(implicit ord: Ordering[S]): Ordering[T] = ???

  // In fact, it's just a contramap, defined in a slightly different way.
  // We supply T => S to receive F[S] => F[T] back.

  // We can use this to get Ordering[Money] for free.

  implicit val moneyOrdering: Ordering[Money] = Ordering.by(_.amount)

  assert(Money(100) < Money(200))

  // Contravariant functors have a natural relationship with subtyping, dual to
  // that of covariant functors.
  class A
  class B extends A

  val b: B = new B
  val a: A = b

  val showA: Show[A] = Show.show(a => s"a!")

  assert(showA.show(a) == "a!")

  val showB1: Show[B] = showA.contramap(b => b: A)

  assert(showB1.show(b) == "a!")

  val showB2: Show[B] = showA.contramap(identity[A])

  assert(showB2.show(b) == "a!")

  // Where B <: A narrow 'narrows' the functor type from A to subtype B.
  val showB3: Show[B] = Contravariant[Show].narrow[A,B](showA)

  assert(showB3.show(b) == "a!")

  // Subtyping relationships are “lifted backwards” by contravariant functors,
  // such that if F is a lawful contravariant functor and B <: A then
  // F[B] <: F[A], which is expressed by Contravariant.narrow.
}
