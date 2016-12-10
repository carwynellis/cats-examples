package uk.carwynellis.cats.datatypes

import cats._
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
}
