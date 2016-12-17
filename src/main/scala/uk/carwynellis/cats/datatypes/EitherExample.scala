package uk.carwynellis.cats.datatypes

import cats._
import cats.implicits._

import scala.annotation.tailrec

/**
  * In day-to-day programming, it is fairly common to find ourselves writing
  * functions that can fail. For instance, querying a service may result in a
  * connection issue, or some unexpected JSON response.
  *
  * To communicate these errors it has become common practice to throw
  * exceptions. However, exceptions are not tracked in any way, shape, or form
  * by the Scala compiler. To see what kind of exceptions (if any) a function
  * may throw, we have to dig through the source code. Then to handle these
  * exceptions, we have to make sure we catch them at the call site. This all
  * becomes even more unwieldy when we try to compose exception-throwing
  * procedures.
  *
  * See http://typelevel.org/cats/datatypes/either.html
  */
object EitherExample extends App {

  def throwsSomeStuff: Int => Double = ???

  def throwsOtherThings: Double => String = ???

  def moreThrowing: String => List[Char] = ???

  def magic = throwsSomeStuff.andThen(throwsOtherThings).andThen(moreThrowing)

  // Assume we happily throw exceptions in our code.  Looking at the types, any
  // of those functions can throw any number of exceptions, we don’t know. When
  // we compose, exceptions from any of the constituent functions can be thrown.
  // Moreover, they may throw the same kind of exception (e.g.
  // IllegalArgumentException) and thus it gets tricky tracking exactly where
  // that exception came from.

  // How then do we communicate an error? By making it explicit in the data type
  // we return.

  // Either vs Validated

  // In general, Validated is used to accumulate errors, while Either is used
  // to short-circuit a computation upon the first error. For more information,
  // see the Validated vs Either section of the Validated documentation.

  // Syntax

  // In Scala 2.10.x and 2.11.x, Either is unbiased. That is, usual combinators
  // like flatMap and map are missing from it. Instead, you call .right or .left
  // to get a RightProjection or LeftProjection (respectively) which does have
  // the combinators. The direction of the projection indicates the direction of
  // bias. For instance, calling map on a RightProjection acts on the Right of
  // an Either.

  val e1: Either[String, Int] = Right(5)

  assert(e1.right.map(_ + 1) == Right(6))

  val e2: Either[String, Int] = Left("Hello")

  assert(e2.right.map(_ + 1) == Left("Hello"))

  // Note the return types are themselves back to Either, so if we want to make
  // more calls to flatMap or map then we again must call right or left.

  // However, the convention is almost always to right-bias Either. Indeed in
  // Scala 2.12.x Either is right-biased by default.

  // More often than not we want to just bias towards one side and call it a day
  // - by convention, the right side is most often chosen. In Scala 2.12.x this
  // convention is implemented in the standard library. Since Cats builds on
  // 2.10.x and 2.11.x, the gaps have been filled via syntax enrichments
  // available under cats.syntax.either._ or cats.implicits._.

  import cats.syntax.either._

  val right: Either[String, Int] = Right(5)

  assert(right.map(_ + 1) == Right(6))

  val left: Either[String, Int] = Left("Hello")

  assert(left.map(_ + 1) == Left("Hello"))

  // For the rest of this tutorial we will assume the syntax enrichment is in
  // scope giving us right-biased Either and a bunch of other useful
  // combinators (both on Either and the companion object).

  // Because Either is right-biased, it is possible to define a Monad instance
  // for it. Since we only ever want the computation to continue in the case of
  // Right, we fix the left type parameter and leave the right one free.

  // Note: the example below assumes usage of the kind-projector compiler plugin and will not compile if it is not being used in a project.

  import cats.Monad

  implicit def eitherMonad[Err]: Monad[Either[Err, ?]] =
    new Monad[Either[Err, ?]] {
      def flatMap[A, B](fa: Either[Err, A])(f: A => Either[Err, B]): Either[Err, B] =
        fa.flatMap(f)

      def pure[A](x: A): Either[Err, A] = Either.right(x)

      @tailrec
      def tailRecM[A, B](a: A)(f: A => Either[Err, Either[A, B]]): Either[Err, B] =
        f(a) match {
          case Right(Right(b)) => Either.right(b)
          case Right(Left(a)) => tailRecM(a)(f)
          // Cast the right type parameter to avoid allocation
          case l@Left(_) => l.rightCast[B]
        }
    }

  // Example usage: Round 1

  // As a running example, we will have a series of functions that will parse a
  // string into an integer, take the reciprocal, and then turn the reciprocal
  // into a string.

  // In exception-throwing code, we would have something like this:

  object ExceptionStyle {
    def parse(s: String): Int =
      if (s.matches("-?[0-9]+")) s.toInt
      else throw new NumberFormatException(s"$s is not a valid integer.")

    def reciprocal(i: Int): Double =
      if (i == 0)
        throw new IllegalArgumentException("Cannot take reciprocal of 0.")
      else 1.0 / i

    def stringify(d: Double): String = d.toString
  }

  // Instead, let’s make the fact that some of our functions can fail explicit
  // in the return type.

  object EitherStyle {
    def parse(s: String): Either[Exception, Int] =
      if (s.matches("-?[0-9]+"))
        Either.right(s.toInt)
      else
        Either.left(new NumberFormatException(s"$s is not a valid integer."))

    def reciprocal(i: Int): Either[Exception, Double] =
      if (i == 0)
        Either.left(new IllegalArgumentException("Cannot take reciprocal of 0."))
      else
        Either.right(1.0 / i)

    def stringify(d: Double): String = d.toString
  }

  // Now, using combinators like flatMap and map, we can compose our functions
  // together.

  import EitherStyle._

  def magic2(s: String): Either[Exception, String] =
    parse(s).flatMap(reciprocal).map(stringify)

  // With the composite function that we actually care about, we can pass in
  // strings and then pattern match on the exception. Because Either is a sealed
  // type (often referred to as an algebraic data type, or ADT), the compiler
  // will complain if we do not check both the Left and Right case.

  lazy val result = magic2("100") match {
    case Left(_: NumberFormatException) => "not a number!"
    case Left(_: IllegalArgumentException) => "can't take reciprocal of 0!"
    case Left(_) => "got unknown exception"
    case Right(s) => s"Got reciprocal: $s"
  }

  assert(result == "Got reciprocal: 0.01")

  // Not bad - if we leave out any of those clauses the compiler will yell at
  // us, as it should. However, note the Left(_) clause - the compiler will
  // complain if we leave that out because it knows that given the type
  // Either[Exception, String], there can be inhabitants of Left that are not
  // NumberFormatException or IllegalArgumentException. However, we “know” by
  // inspection of the source that those will be the only exceptions thrown, so
  // it seems strange to have to account for other exceptions. This implies
  // that there is still room to improve.
}
