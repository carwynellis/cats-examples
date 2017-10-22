package uk.carwynellis.cats.datatypes

import cats.implicits._
import cats.data.OptionT

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.language.postfixOps

/**
  * OptionT[F[_], A] is a light wrapper around an F[Option[A].
  *
  * Strictly speaking it is a monad transformer for Option.
  *
  * OptionT can be more convenient than using F[Option[A]] directly.
  *
  * See http://typelevel.org/cats/datatypes/optiont.html
  *
  * Also
  */
object OptionTExample extends App {

  // OptionT can be used to reduce map boiler plate.

  // Consider the following
  val greeting: Future[Option[String]]
    = Future.successful(Some("Welcome back, Foo"))

  // Mapping over the future and the option adds some boiler plate...

  val excitedGreeting = greeting.map(_.map(_ + "!"))

  val hasWelcome = greeting.map(_.filter(_.contains("Welcome")))

  val withFallback = greeting.map(_.getOrElse("Hello there!"))

  // As you can see the basic structure is the same in that we need to map over
  // the future in order to access the Option contained within.

  // OptionT can help to remove some of this boilerplate. It exposes methods
  // that look like those on Option, but which handle the outer map call on
  // the Future for us.

  val greetingT: OptionT[Future, String] = OptionT(greeting)

  val excitedGreeting2 = greetingT.map(_ + "!")

  val hasWelcome2 = greetingT.filter(_.contains("Welcome"))

  val withFallback2 = greetingT.getOrElse("Hello there!")

  // From Option[A] and/or F[A] to OptionT[F, A]

  // Sometimes you may have an Option[A] and, or an F[A] and want to lift them
  // into an Option[F, A]. OptionT exposes two useful methods, fromOption and
  // liftF for this purpose.

  val greetingFO: Future[Option[String]] = Future.successful(Some("Hello"))

  val firstnameF: Future[String] = Future.successful("Jane")

  val lastnameO: Option[String] = Some("Doe")

  val ot: OptionT[Future, String] = for {
    g <- OptionT(greetingFO)
    f <- OptionT.liftF(firstnameF)
    l <- OptionT.fromOption[Future](lastnameO)
  } yield s"$g $f $l"

  assert(Await.result(ot.value, 1 second) contains "Hello Jane Doe")

  // From A to OptionT[F,A]

  // If you only have an A and you wish to lift it into an Option[F,A],
  // assuming you have an applicative instance for F, you can use some which is
  // an alias for pure. There is also a none method which can be used to create
  // an OptionT where the Option wrapped A is actually a None.

  val greet: OptionT[Future, String] = OptionT.pure("Hola!")

  val greetAlt: OptionT[Future, String] = OptionT.some("Hi!")

  val emptyGreet: OptionT[Future, String] = OptionT.none

  // Beyond map

  // Sometimes the operation you want to perform on a Future[Option[String]]
  // might not be as simple as just wrapping the Option method in a Future.map.
  // For example what if we need to fallback to a default greeting if no value
  // exists? Without OptionT this might look like

  val defaultGreeting: Future[String] = Future.successful("Welcome")

  val greetingWithFallback: Future[String] =
    greeting.flatMap { g: Option[String] =>
      g.map(Future.successful).getOrElse(defaultGreeting)
    }

  // We can't make use of the getOrElse method on OptionT because it takes a
  // default value of type A instead of a Future[A]. However the getOrElseF
  // method does exactly what we want in this instance.

  val greetingWithFallback2: Future[String] =
    greetingT.getOrElseF(defaultGreeting)

  // Getting to the underlying instance

  // To retrieve the F[Option[A]] from an OptionT call value.

  val greetingValue: Future[Option[String]] = greetingT.value
}
